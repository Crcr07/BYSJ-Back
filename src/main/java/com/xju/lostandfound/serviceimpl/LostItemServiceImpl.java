package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xju.lostandfound.common.utils.AliOssUtil; // 🌟 注入 OSS 工具类
import com.xju.lostandfound.common.utils.FileUtils;
import com.xju.lostandfound.common.utils.ImageFeatureUtils;
import com.xju.lostandfound.common.utils.OcrUtils;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.mapper.LostItemMapper;
import com.xju.lostandfound.model.dto.PublishItemDto;
import com.xju.lostandfound.service.LostItemService;
import com.xju.lostandfound.service.MatchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 失物业务实现类 - 已修复文件上传顺序问题
 */
@Service
public class LostItemServiceImpl extends ServiceImpl<LostItemMapper, LostItem> implements LostItemService {

    @Value("${campus.file-upload-path}")
    private String uploadPath;

    @Autowired
    private OcrUtils ocrUtils;

    @Autowired
    private MatchRecordService matchRecordService;

    @Autowired
    private AliOssUtil aliOssUtil; // 🌟 注入阿里云 OSS 工具类

    @Override
    public boolean publish(PublishItemDto dto, Long userId) {
        String fileName = null;
        File uploadedFile = null;
        String ossUrl = null;

        LostItem lostItem = new LostItem();
        lostItem.setUserId(userId);
        lostItem.setItemName(dto.getItemName());
        lostItem.setCategoryId(dto.getCategoryId());
        lostItem.setLostLocation(dto.getLocation());
        lostItem.setDescription(dto.getDescription());

        // ============================================================
        // 🌟 修复核心逻辑：调换上传顺序，解决 NoSuchFileException
        // ============================================================
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            // 1. 先进行 OSS 上传：此操作通过 getInputStream() 读取流，不会移动或销毁临时文件
            try {
                ossUrl = aliOssUtil.upload(dto.getFile());
                lostItem.setImageUrl(ossUrl); // 将 OSS 返回的完整 URL 存入数据库
            } catch (Exception e) {
                // 捕获日志中的上传失败异常
                throw new RuntimeException("文件上传至阿里云OSS失败", e);
            }

            // 2. OSS 成功后，再调用 FileUtils 保存到本地副本：用于 OCR 和特征提取
            // 注意：FileUtils.upload 内部调用 transferTo，之后 Tomcat 的临时文件会被销毁
            fileName = FileUtils.upload(dto.getFile(), uploadPath);
            uploadedFile = new File(new File(uploadPath).getAbsoluteFile(), fileName);
        }

        // 处理丢失时间格式
        if (dto.getDate() != null && !dto.getDate().isEmpty()) {
            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                lostItem.setLostTime(LocalDateTime.parse(dto.getDate(), df));
            } catch (Exception e) {
                lostItem.setLostTime(LocalDateTime.now());
            }
        } else {
            lostItem.setLostTime(LocalDateTime.now());
        }

        lostItem.setCreateTime(LocalDateTime.now());
        lostItem.setStatus(0);

        // 提取图片文字和特征 (基于本地保存的副本进行 AI 处理)
        if (uploadedFile != null && uploadedFile.exists()) {
            String text = ocrUtils.doOcr(uploadedFile);
            String feature = ImageFeatureUtils.getImageFingerprint(uploadedFile);

            System.out.println("====== 毕设算法调试日志 ======");
            System.out.println("文件绝对路径: " + uploadedFile.getAbsolutePath());
            System.out.println("OCR 识别结果: [" + text + "]");
            System.out.println("图像特征指纹: [" + feature + "]");
            System.out.println("============================");

            lostItem.setOcrText(text == null ? "" : text);
            lostItem.setImageFeature(feature == null ? "" : feature);
        }

        // ==========================================
        // 🌟 核心：保存数据库记录并触发智能匹配
        // ==========================================
        boolean isSaved = this.save(lostItem);
        if (isSaved) {
            matchRecordService.matchAfterLostPublished(lostItem);
        }

        return isSaved;
    }
}