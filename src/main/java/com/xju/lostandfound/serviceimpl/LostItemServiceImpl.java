package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xju.lostandfound.common.utils.AliOssUtil;
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
import java.util.concurrent.CompletableFuture;

@Service
public class LostItemServiceImpl extends ServiceImpl<LostItemMapper, LostItem> implements LostItemService {

    @Value("${campus.file-upload-path}")
    private String uploadPath;

    @Autowired
    private OcrUtils ocrUtils;

    @Autowired
    private MatchRecordService matchRecordService;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Override
    public boolean publish(PublishItemDto dto, Long userId) {
        // 1. 初始化失物实体信息
        LostItem lostItem = new LostItem();
        lostItem.setUserId(userId);
        lostItem.setItemName(dto.getItemName());
        lostItem.setCategoryId(dto.getCategoryId());
        lostItem.setLostLocation(dto.getLocation());
        lostItem.setDescription(dto.getDescription());
        lostItem.setCreateTime(LocalDateTime.now());
        lostItem.setStatus(0);

        // 初始化空特征，防止数据库字段为null引发异常
        lostItem.setOcrText("");
        lostItem.setImageFeature("");

        // 处理时间格式
        if (dto.getDate() != null && !dto.getDate().isEmpty()) {
            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                lostItem.setLostTime(LocalDateTime.parse(dto.getDate(), df));
            } catch (Exception e) {
                lostItem.setLostTime(LocalDateTime.now());
            }
        }

        // 2. 处理文件上传（同步执行，因为需要立刻获得图片URL展示给前端）
        File localFile = null;
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            try {
                // 上传至 OSS
                String ossUrl = aliOssUtil.upload(dto.getFile());
                lostItem.setImageUrl(ossUrl);

                // 保存本地临时副本用于后续 AI 提取
                String fileName = FileUtils.upload(dto.getFile(), uploadPath);
                localFile = new File(new File(uploadPath).getAbsoluteFile(), fileName);
            } catch (Exception e) {
                throw new RuntimeException("文件上传处理失败", e);
            }
        }

        // 3. 立即保存基础信息到数据库
        boolean isSaved = this.save(lostItem);

        // 4. 🌟 异步处理核心：开启后台线程执行 AI 提取与匹配
        if (isSaved) {
            final File finalFile = localFile; // 匿名内部类引用
            CompletableFuture.runAsync(() -> {
                try {
                    // (1) 在后台执行 OCR 和特征提取 (耗时操作)
                    if (finalFile != null && finalFile.exists()) {
                        String text = ocrUtils.doOcr(finalFile);
                        String feature = ImageFeatureUtils.getImageFingerprint(finalFile);

                        lostItem.setOcrText(text == null ? "" : text);
                        lostItem.setImageFeature(feature == null ? "" : feature);

                        // (2) 提取完成后更新数据库记录
                        this.updateById(lostItem);
                    }

                    // (3) 调用大模型进行异步匹配
                    matchRecordService.matchAfterLostPublished(lostItem);

                } catch (Exception e) {
                    System.err.println("后台异步处理任务发生异常: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        // 返回 true，前端会立刻收到响应并跳转页面
        return isSaved;
    }
}