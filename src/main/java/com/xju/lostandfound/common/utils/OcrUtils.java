package com.xju.lostandfound.common.utils;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OCR 工具类
 * 负责调用 Tesseract 引擎进行文字识别
 */
@Component // 交给 Spring 管理，方便读取配置文件
public class OcrUtils {

    @Value("${campus.tessdata-path}")
    private String tessDataPath; // 从 yml 读取 D:/tessdata/

    /**
     * 识别图片中的文字
     * @param imageFile 图片文件
     * @return 识别出的文本
     */
//    public String doOcr(File imageFile) {
//        // 1. 创建 Tesseract 实例
//        ITesseract instance = new Tesseract();
//
//        // 2. 设置训练数据文件夹路径
//        instance.setDatapath(tessDataPath);
//
//        // 3. 设置语言 (chi_sim: 简体中文, eng: 英文)
//        instance.setLanguage("chi_sim+eng");
//
//        try {
//            // 4. 开始识别
//            String result = instance.doOCR(imageFile);
//            // 去除多余的空格和换行，方便存储和匹配
//            return result.replaceAll("\\s+", " ").trim();
//        } catch (TesseractException e) {
//            e.printStackTrace();
//            return ""; // 识别失败返回空字符串，不影响主流程
//        }
//    }
    // ... 前面的代码不变

    public String doOcr(File imageFile) {
        ITesseract instance = new Tesseract();

        // 🌟 修改点：将相对路径转换为绝对路径，防止 Tesseract 找不到文件
        // 如果 tessDataPath 是 "../tessdata/"，这就变成了 "C:\...\tessdata\"
        File tessDataFolder = new File(tessDataPath);
        instance.setDatapath(tessDataFolder.getAbsolutePath());

        instance.setLanguage("chi_sim+eng");

        try {
            String result = instance.doOCR(imageFile);
            return result.replaceAll("\\s+", " ").trim();
        } catch (TesseractException e) {
            e.printStackTrace();
            return "";
        }
    }


}