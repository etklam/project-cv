package me.hker.module.reward.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import me.hker.module.reward.entity.PromoCode
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface PromoCodeMapper : BaseMapper<PromoCode> {
    @Select("SELECT * FROM promo_code WHERE code = #{code} LIMIT 1 FOR UPDATE")
    fun selectForUpdate(wrapper: com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PromoCode>): PromoCode?
}
