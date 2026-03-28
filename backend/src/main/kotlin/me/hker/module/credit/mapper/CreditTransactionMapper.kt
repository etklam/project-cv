package me.hker.module.credit.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.module.credit.entity.CreditTransaction
import org.apache.ibatis.annotations.Mapper

@Mapper
interface CreditTransactionMapper : BaseMapper<CreditTransaction> {
    fun findByReferenceTypeAndReferenceId(
        referenceType: String,
        referenceId: Long
    ): CreditTransaction? {
        return selectOne(
            QueryWrapper<CreditTransaction>()
                .eq("reference_type", referenceType)
                .eq("reference_id", referenceId)
                .orderByDesc("created_at")
                .last("LIMIT 1")
        )
    }

    fun findByReferenceTypeAndDescription(
        referenceType: String,
        description: String
    ): CreditTransaction? {
        return selectOne(
            QueryWrapper<CreditTransaction>()
                .eq("reference_type", referenceType)
                .eq("description", description)
                .orderByDesc("created_at")
                .last("LIMIT 1")
        )
    }
}
