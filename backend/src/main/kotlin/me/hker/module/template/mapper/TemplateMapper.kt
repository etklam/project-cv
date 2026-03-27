package me.hker.module.template.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import me.hker.module.template.entity.Template
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface TemplateMapper : BaseMapper<Template> {
    @Select(
        """
        SELECT *
        FROM templates
        WHERE is_active = true
        ORDER BY sort_order ASC, id ASC
        """,
    )
    fun selectActiveTemplates(): List<Template>
}
