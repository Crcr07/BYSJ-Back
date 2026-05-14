package com.xju.lostandfound.model.vo;

import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 匹配记录的视图展示对象 (返回给前端的最终格式)
 */
@Data
public class MatchRecordVo {
    private Long recordId;         // 匹配记录的ID
    private Double matchScore;     // 匹配得分 (例如 1.0 表示 100%)
    private Integer status;        // 状态 0:待确认 1:已确认匹配 2:已否决
    private LocalDateTime createTime; // 匹配生成时间

    private LostItem lostItem;     // 关联的失物详细信息
    private FoundItem foundItem;   // 关联的招领详细信息

    private String matchReason;
    private String matchedFields;
    private String riskLevel;
}