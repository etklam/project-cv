package me.hker.module.cv.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import me.hker.module.cv.entity.CvSection
import org.apache.ibatis.annotations.Mapper

@Mapper
interface CvSectionMapper : BaseMapper<CvSection>
