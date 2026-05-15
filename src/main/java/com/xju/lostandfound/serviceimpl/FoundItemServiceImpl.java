package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xju.lostandfound.common.utils.AliOssUtil;
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
import java.util.concurrent.CompletableFuture;

@Service
public class FoundItemServiceImpl extends ServiceImpl<FoundItemMapper, FoundItem> implements FoundItemService {

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
        FoundItem foundItem = new FoundItem();
        foundItem.setUserId(userId);
        foundItem.setItemName(dto.getItemName());
        foundItem.setCategoryId(dto.getCategoryId());
        foundItem.setFoundLocation(dto.getLocation());
        foundItem.setDescription(dto.getDescription());
        foundItem.setCreateTime(LocalDateTime.now());
        foundItem.setStatus(0);

        foundItem.setOcrText("");
        foundItem.setImageFeature("");

        if (dto.getDate() != null && !dto.getDate().isEmpty()) {
            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                foundItem.setFoundTime(LocalDateTime.parse(dto.getDate(), df));
            } catch (Exception e) {
                foundItem.setFoundTime(LocalDateTime.now());
            }
        }

        File localFile = null;
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            try {
                String ossUrl = aliOssUtil.upload(dto.getFile());
                foundItem.setImageUrl(ossUrl);

                String fileName = FileUtils.upload(dto.getFile(), uploadPath);
                localFile = new File(new File(uploadPath).getAbsoluteFile(), fileName);
            } catch (Exception e) {
                throw new RuntimeException("文件上传处理失败", e);
            }
        }

        boolean isSaved = this.save(foundItem);

        if (isSaved) {
            final File finalFile = localFile;
            CompletableFuture.runAsync(() -> {
                try {
                    // 后台执行耗时任务
                    if (finalFile != null && finalFile.exists()) {
                        String text = ocrUtils.doOcr(finalFile);
                        String feature = ImageFeatureUtils.getImageFingerprint(finalFile);

                        foundItem.setOcrText(text == null ? "" : text);
                        foundItem.setImageFeature(feature == null ? "" : feature);

                        this.updateById(foundItem);
                    }

                    // 异步调用匹配逻辑
                    matchRecordService.matchAfterFoundPublished(foundItem);

                } catch (Exception e) {
                    System.err.println("后台异步处理任务发生异常: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        return isSaved;
    }
}