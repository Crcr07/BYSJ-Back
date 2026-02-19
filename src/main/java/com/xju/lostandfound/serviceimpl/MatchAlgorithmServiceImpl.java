package com.xju.lostandfound.serviceimpl;

import com.xju.lostandfound.common.utils.ImageFeatureUtils;
import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.service.MatchAlgorithmService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class MatchAlgorithmServiceImpl implements MatchAlgorithmService {

    // 默认权重配置：图像特征占 60%，OCR文本占 40%
    private static final double WEIGHT_IMAGE = 0.6;
    private static final double WEIGHT_TEXT = 0.4;

    @Override
    public double calculateMatchScore(LostItem lostItem, FoundItem foundItem) {
        if (lostItem == null || foundItem == null) {
            return 0.0;
        }

        // 1. 计算图像特征相似度
        double imageScore = 0.0;
        boolean hasImage = isValidString(lostItem.getImageFeature()) && isValidString(foundItem.getImageFeature());
        if (hasImage) {
            imageScore = ImageFeatureUtils.calculateSimilarity(lostItem.getImageFeature(), foundItem.getImageFeature());
        }

        // 2. 计算 OCR 文本特征相似度 (使用 Jaccard 相似度)
        double textScore = 0.0;
        boolean hasText = isValidString(lostItem.getOcrText()) && isValidString(foundItem.getOcrText());
        if (hasText) {
            textScore = calculateTextSimilarity(lostItem.getOcrText(), foundItem.getOcrText());
        }

        // 3. 动态权重计算总得分
        if (hasImage && hasText) {
            // 图文都有，按照设定的比例加权
            return (imageScore * WEIGHT_IMAGE) + (textScore * WEIGHT_TEXT);
        } else if (hasImage) {
            // 只有图片，完全依赖图像相似度
            return imageScore;
        } else if (hasText) {
            // 只有文本，完全依赖文本相似度
            return textScore;
        }

        // 都没有，匹配度为0
        return 0.0;
    }

    /**
     * 计算文本相似度 (Jaccard 相似度算法实现)
     * 公式: 交集数量 / 并集数量
     */
    private double calculateTextSimilarity(String text1, String text2) {
        // 将文本拆分为单字集合 (针对中文优化)
        Set<Character> set1 = toCharSet(text1);
        Set<Character> set2 = toCharSet(text2);

        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        // 计算交集
        Set<Character> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // 计算并集
        Set<Character> union = new HashSet<>(set1);
        union.addAll(set2);

        // 返回 Jaccard 指数
        return (double) intersection.size() / union.size();
    }

    /**
     * 辅助方法：将字符串转为字符集合（去除空格）
     */
    private Set<Character> toCharSet(String str) {
        Set<Character> set = new HashSet<>();
        if (str != null) {
            for (char c : str.toCharArray()) {
                if (!Character.isWhitespace(c)) {
                    set.add(c);
                }
            }
        }
        return set;
    }

    /**
     * 辅助方法：判空
     */
    private boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }
}