package com.xju.lostandfound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xju.lostandfound.entity.CreditRecord;

public interface CreditRecordService extends IService<CreditRecord> {

    /**
     * 核心方法：统一处理用户积分的增减，并自动记录流水
     * @param userId 用户ID
     * @param amount 变动数值 (正数为加分，负数为扣分)
     * @param reason 变动原因
     * @return 是否成功
     */
    boolean changeUserCredit(Long userId, Integer amount, String reason);
}