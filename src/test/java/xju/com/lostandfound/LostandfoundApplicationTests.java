package com.xju.lostandfound;

import com.xju.lostandfound.entity.FoundItem;
import com.xju.lostandfound.entity.LostItem;
import com.xju.lostandfound.service.MatchAlgorithmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LostandfoundApplicationTests {

	@Autowired
	private MatchAlgorithmService matchAlgorithmService;

	@Test
	void testMatchAlgorithm() {
		// 模拟数据库里查出来的失物
		LostItem lost = new LostItem();
		lost.setItemName("校园卡");
		lost.setImageFeature("1111000011110000111100001111000011110000111100001111000011110000"); // 64位哈希
		lost.setOcrText("新疆大学 学生证 姓名:张三 学号:2022001");

		// 模拟刚刚发布的招领物品
		FoundItem found = new FoundItem();
		found.setItemName("一张卡片");
		// 假设图片有一点点光线差异，导致哈希变了几个位
		found.setImageFeature("1111000011110000111100001111000011110000111100001111000010100101");
		found.setOcrText("新疆大学 姓名张三 2022001"); // 识别有残缺

		// 开始计算
		double score = matchAlgorithmService.calculateMatchScore(lost, found);

		System.out.println("==================================");
		System.out.println("经过多模态算法计算，两者的匹配度为: " + (score * 100) + "%");
		System.out.println("==================================");
	}
}