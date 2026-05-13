// 文件路径：src/main/java/com/xju/lostandfound/model/dto/CozeMatchResultDto.java

package com.xju.lostandfound.model.dto;

import lombok.Data;

@Data
public class CozeMatchResultDto {
    private String matchedFields;
    private String output;
    private String reason;
    private String riskLevel;
}