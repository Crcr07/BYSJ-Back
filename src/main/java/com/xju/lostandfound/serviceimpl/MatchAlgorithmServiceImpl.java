package com.xju.lostandfound.serviceimpl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.entity.MatchRecord;
import com.xju.lostandfound.model.dto.CozeMatchResultDto;
import com.xju.lostandfound.service.MatchAlgorithmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MatchAlgorithmServiceImpl implements MatchAlgorithmService {

    @Value("${coze.api.url}")
    private String apiUrl;

    @Value("${coze.api.token}")
    private String apiToken;

    @Value("${coze.workflow.id}")
    private String workflowId;

    private final RestTemplate restTemplate;

    public MatchAlgorithmServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public MatchRecord calculateMatchScore(LostItem lostItem, FoundItem foundItem) {
        Map<String, Object> parameters = new HashMap<>();

        // 🌟 关键修改点 1：这里的参数名必须严格叫做 lostItem 和 foundItem
        parameters.put("lostItem", buildLostItemMap(lostItem));
        parameters.put("foundItem", buildFoundItemMap(foundItem));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("workflow_id", workflowId);
        requestBody.put("parameters", parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        System.out.println("\n⏳ 正在请求 Coze 大模型进行比对: 招领 [" + foundItem.getItemName() + "] VS 失物 [" + lostItem.getItemName() + "]");

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseCozeResponse(response.getBody(), lostItem.getId(), foundItem.getId());
            } else {
                System.out.println("❌ Coze 接口 HTTP 状态码异常: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ 调用大模型工作流异常", e);
        }
        return null;
    }

    private MatchRecord parseCozeResponse(String responseBody, Long lostId, Long foundId) {
        JSONObject jsonObject = JSON.parseObject(responseBody);
        if (jsonObject.getIntValue("code") == 0) {
            String dataStr = jsonObject.getString("data");
            CozeMatchResultDto resultDto = JSON.parseObject(dataStr, CozeMatchResultDto.class);

            MatchRecord record = new MatchRecord();
            record.setLostId(lostId);
            record.setFoundId(foundId);

            double score = Double.parseDouble(resultDto.getOutput());
            record.setMatchScore(score * 100);

            record.setMatchReason(resultDto.getReason());
            record.setMatchedFields(resultDto.getMatchedFields());
            record.setRiskLevel(resultDto.getRiskLevel());
            record.setCreateTime(LocalDateTime.now());

            System.out.println("📊 大模型判定得分: " + score + " | 理由: " + resultDto.getReason());

            if (score >= 0.7) {
                record.setStatus(0);
                System.out.println("🎉 得分 >= 0.7，匹配达标！系统已生成匹配记录。\n");
            } else {
                System.out.println("📉 得分低于阈值 0.7，系统判定为不匹配，过滤丢弃。\n");
            }

            return record;
        } else {
            System.out.println("❌ Coze 业务级报错: " + jsonObject.getString("msg"));
        }
        return null;
    }

    // ==========================================
    // 🌟 关键修改点 2：将实体类精准转换为 Coze 要求的 7个 String 字段
    // ==========================================

    private Map<String, String> buildLostItemMap(LostItem item) {
        Map<String, String> map = new HashMap<>();
        map.put("name", item.getItemName() != null ? item.getItemName() : "");
        map.put("category", item.getCategoryId() != null ? String.valueOf(item.getCategoryId()) : "");
        map.put("ocrText", item.getOcrText() != null ? item.getOcrText() : "");
        map.put("imageUrl", item.getImageUrl() != null ? item.getImageUrl() : "");
        map.put("location", item.getLostLocation() != null ? item.getLostLocation() : "");
        map.put("time", item.getLostTime() != null ? item.getLostTime().toString() : "");
        map.put("description", item.getDescription() != null ? item.getDescription() : "");
        return map;
    }

    private Map<String, String> buildFoundItemMap(FoundItem item) {
        Map<String, String> map = new HashMap<>();
        map.put("name", item.getItemName() != null ? item.getItemName() : "");
        map.put("category", item.getCategoryId() != null ? String.valueOf(item.getCategoryId()) : "");
        map.put("ocrText", item.getOcrText() != null ? item.getOcrText() : "");
        map.put("imageUrl", item.getImageUrl() != null ? item.getImageUrl() : "");
        map.put("location", item.getFoundLocation() != null ? item.getFoundLocation() : "");
        map.put("time", item.getFoundTime() != null ? item.getFoundTime().toString() : "");
        map.put("description", item.getDescription() != null ? item.getDescription() : "");
        return map;
    }
}