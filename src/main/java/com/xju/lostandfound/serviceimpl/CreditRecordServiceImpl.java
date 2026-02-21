package com.xju.lostandfound.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xju.lostandfound.entity.CreditRecord;
import com.xju.lostandfound.entity.User;
import com.xju.lostandfound.mapper.CreditRecordMapper;
import com.xju.lostandfound.service.CreditRecordService;
import com.xju.lostandfound.service.NoticeService;
import com.xju.lostandfound.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreditRecordServiceImpl extends ServiceImpl<CreditRecordMapper, CreditRecord> implements CreditRecordService {

    @Autowired
    @Lazy
    private CreditRecordService creditRecordService;

    @Autowired
    @Lazy // 加上 Lazy 防止循环依赖
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class) // 🌟 开启事务，保证更新总分和插入流水同时成功或失败
    public boolean changeUserCredit(Long userId, Integer amount, String reason) {
        // 1. 获取用户
        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }

        // 2. 计算新积分 (防止超过上限或跌破下限)
        int currentScore = user.getCreditScore() != null ? user.getCreditScore() : 100;
        int newScore = currentScore + amount;

        if (newScore > 150) newScore = 150; // 设置最高分上限 150
        if (newScore < 0) newScore = 0;     // 设置最低分下限 0

        // 3. 计算实际变动的分数 (比如原来 145，加 10 分，实际只能加 5 分)
        int actualChange = newScore - currentScore;
        if (actualChange == 0) {
            return true; // 如果积分没有实际变动，就不记录流水了
        }

        // 4. 更新用户表总分
        user.setCreditScore(newScore);
        userService.updateById(user);

        // 5. 插入流水记录到明细表
        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeAmount(actualChange);
        record.setReason(reason);
        record.setCreateTime(LocalDateTime.now());
        this.save(record); // 👈 日志执行到了这里

        // ================= 🌟 请确保下面这段代码真的在这里！ =================
        if (actualChange > 0) {
            noticeService.sendNotice(userId, "🏆 信用积分奖励", "感谢您的帮助！因【" + reason + "】，系统已为您发放 " + actualChange + " 点积分！", 2);
        } else if (actualChange < 0) {
            noticeService.sendNotice(userId, "⚠️ 信用分扣除通知", "您的信用分减少了 " + Math.abs(actualChange) + " 分，原因：" + reason, 3);
        }
        // =========================================================================

        return true;
    }
}