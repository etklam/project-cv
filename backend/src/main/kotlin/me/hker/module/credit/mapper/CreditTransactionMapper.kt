package me.hker.module.credit.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import me.hker.module.credit.entity.CreditTransaction
import org.apache.ibatis.annotations.Mapper

@Mapper
interface CreditTransactionMapper : BaseMapper<CreditTransaction>
