package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xju.lostandfound.common.utils.AliOssUtil; // 🌟 注入 OSS 工具类
import com.xju.lostandfound.common.utils.FileUtils;
import com.xju.lostandfound.common.utils.ImageFeatureUtils;
import com.xju.lostandfound.common.utils.OcrUtils;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.mapper.FoundItemMapper;
import com.xju.lostandfound.model.dto.PublishItemDto;
import com.xju.lostandfound.service.FoundItemService;
import com.xju.lostandfound.service.MatchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FoundItemServiceImpl extends ServiceImpl<FoundItemMapper, FoundItem> implements FoundItemService {

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

        FoundItem foundItem = new FoundItem();
        foundItem.setUserId(userId);
        foundItem.setItemName(dto.getItemName());
        foundItem.setCategoryId(dto.getCategoryId());
        foundItem.setFoundLocation(dto.getLocation()); // 映射为拾取地点
        foundItem.setDescription(dto.getDescription());

        // ============================================================
        // 🌟 修复核心逻辑：调换上传顺序，解决 NoSuchFileException
        // ============================================================
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            // 1. 先进行 OSS 上传：此操作通过 getInputStream() 读取流，不会移动或销毁临时文件
            try {
                ossUrl = aliOssUtil.upload(dto.getFile());
                foundItem.setImageUrl(ossUrl); // 将 OSS 返回的完整 URL 存入数据库
            } catch (Exception e) {
                // 捕获日志中的上传失败异常
                throw new RuntimeException("文件上传至阿里云OSS失败", e);
            }

            // 2. OSS 成功后，再调用 FileUtils 保存到本地副本：用于 OCR 和特征提取
            // 注意：FileUtils.upload 内部调用 transferTo，之后 Tomcat 的临时文件会被销毁
            fileName = FileUtils.upload(dto.getFile(), uploadPath);
            uploadedFile = new File(new File(uploadPath).getAbsoluteFile(), fileName);
        }

        if (dto.getDate() != null && !dto.getDate().isEmpty()) {
            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                foundItem.setFoundTime(LocalDateTime.parse(dto.getDate(), df));
            } catch (Exception e) {
                foundItem.setFoundTime(LocalDateTime.now());
            }
        } else {
            foundItem.setFoundTime(LocalDateTime.now());
        }

        foundItem.setCreateTime(LocalDateTime.now());
        foundItem.setStatus(0);

        // 提取图片文字和特征
        if (uploadedFile != null && uploadedFile.exists()) {
            String text = ocrUtils.doOcr(uploadedFile);
            String feature = ImageFeatureUtils.getImageFingerprint(uploadedFile);

            System.out.println("====== 毕设算法调试日志 ======");
            System.out.println("文件绝对路径: " + uploadedFile.getAbsolutePath());
            System.out.println("OCR 识别结果: [" + text + "]");
            System.out.println("图像特征指纹: [" + feature + "]");
            System.out.println("============================");

            // 存入实体类 (如果为空字符串，我们也存进去，方便排查)
            foundItem.setOcrText(text == null ? "" : text);
            foundItem.setImageFeature(feature == null ? "" : feature);

        } else {
            System.out.println("【警告】系统找不到上传的图片文件！");
        }

        // ==========================================
        // 🌟 核心：保存并触发匹配
        // ==========================================
        boolean isSaved = this.save(foundItem);
        if (isSaved) {
            // 这里传入的是 foundItem (招领对象)
            matchRecordService.matchAfterFoundPublished(foundItem);
        }

        return isSaved;
    }
}