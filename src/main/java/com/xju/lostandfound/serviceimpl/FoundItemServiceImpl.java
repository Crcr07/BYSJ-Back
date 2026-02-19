package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    // 🌟 注入刚刚写好的智能匹配服务
    @Autowired
    private MatchRecordService matchRecordService;

    @Override
    public boolean publish(PublishItemDto dto, Long userId) {
        String fileName = null;
        File uploadedFile = null;

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            fileName = FileUtils.upload(dto.getFile(), uploadPath);
            uploadedFile = new File(new File(uploadPath).getAbsoluteFile(), fileName);
        }

        FoundItem foundItem = new FoundItem();
        foundItem.setUserId(userId);
        foundItem.setItemName(dto.getItemName());
        foundItem.setCategoryId(dto.getCategoryId());
        foundItem.setFoundLocation(dto.getLocation()); // 映射为拾取地点
        foundItem.setDescription(dto.getDescription());
        foundItem.setImageUrl(fileName);

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

        if (uploadedFile != null && uploadedFile.exists()) {
            foundItem.setOcrText(ocrUtils.doOcr(uploadedFile));
            foundItem.setImageFeature(ImageFeatureUtils.getImageFingerprint(uploadedFile));
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