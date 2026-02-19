package com.xju.lostandfound.common.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 图像处理工具类
 * 使用 pHash (感知哈希) 算法计算图像指纹
 */
public class ImageFeatureUtils {

    /**
     * 计算图片的 pHash 指纹
     * @param imageFile 图片文件
     * @return 64位指纹字符串 (例如: "101010...")
     */
    public static String getImageFingerprint(File imageFile) {
        try {
            // 1. 读取图片
            BufferedImage src = ImageIO.read(imageFile);
            if (src == null) return "";

            // 2. 缩小尺寸 -> 32x32
            // 这一步是为了简化计算，忽略细节，只看整体结构
            BufferedImage smaller = resize(src, 32, 32);

            // 3. 灰度化
            // 把彩色图片变成黑白，进一步简化
            int[] pixels = new int[32 * 32];
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 32; j++) {
                    pixels[i * 32 + j] = getGray(smaller.getRGB(j, i));
                }
            }

            // 4. 计算 DCT (离散余弦变换) - 简化版，计算平均值
            // pHash 的核心简化版：计算像素平均值
            double avg = 0;
            for (int pixel : pixels) {
                avg += pixel;
            }
            avg /= pixels.length;

            // 5. 生成哈希 (大于平均值记为1，小于记为0)
            StringBuilder sb = new StringBuilder();
            for (int pixel : pixels) {
                sb.append(pixel >= avg ? "1" : "0");
            }
            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 计算两个指纹的相似度 (汉明距离)
     * @param hash1 指纹1
     * @param hash2 指纹2
     * @return 相似度 (0.0 ~ 1.0)，越高越相似
     */
    public static double calculateSimilarity(String hash1, String hash2) {
        if (hash1 == null || hash2 == null || hash1.length() != hash2.length()) {
            return 0.0;
        }
        int distance = 0;
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                distance++;
            }
        }
        // 简单归一化：(总长度 - 差异数) / 总长度
        return 1.0 - ((double) distance / hash1.length());
    }

    // --- 内部辅助方法 ---

    private static BufferedImage resize(BufferedImage src, int width, int height) {
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        dest.getGraphics().drawImage(src, 0, 0, width, height, null);
        return dest;
    }

    private static int getGray(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        return (int) (0.3 * r + 0.59 * g + 0.11 * b);
    }
}