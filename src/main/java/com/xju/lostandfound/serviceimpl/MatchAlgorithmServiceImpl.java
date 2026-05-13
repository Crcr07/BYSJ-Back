
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

import java.math.BigDecimal;
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
        // 此处的键名需严格对应你 Coze 工作流“起始节点”定义的参数名
        parameters.put("lost_item_info", buildItemMap(lostItem));
        parameters.put("found_item_info", buildItemMap(foundItem));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("workflow_id", workflowId);
        requestBody.put("parameters", parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseCozeResponse(response.getBody(), lostItem.getId(), foundItem.getId());
            }
        } catch (Exception e) {
            log.error("调用大模型工作流异常", e);
        }
        return null;
    }

    private MatchRecord parseCozeResponse(String responseBody, Long lostId, Long foundId) {
        JSONObject jsonObject = JSON.parseObject(responseBody);
        if (jsonObject.getIntValue("code") == 0) {
            // 取出 Coze 实际返回的业务 JSON 字符串
            String dataStr = jsonObject.getString("data");
            CozeMatchResultDto resultDto = JSON.parseObject(dataStr, CozeMatchResultDto.class);

            MatchRecord record = new MatchRecord();
            record.setLostId(lostId);
            record.setFoundId(foundId);

            // 【关键修改点 1】：使用 Double.parseDouble 替代 BigDecimal
            record.setMatchScore(Double.parseDouble(resultDto.getOutput()) * 100);

            record.setMatchReason(resultDto.getReason());
            record.setMatchedFields(resultDto.getMatchedFields());
            record.setRiskLevel(resultDto.getRiskLevel());
            record.setCreateTime(LocalDateTime.now());

            // 【关键修改点 2】：阈值判断也使用 Double
            if (Double.parseDouble(resultDto.getOutput()) >= 0.7) {
                record.setStatus(0);
            }

            return record;
        }
        return null;
    }

    private Map<String, Object> buildItemMap(Object item) {
        return JSON.parseObject(JSON.toJSONString(item), Map.class);
    }
}