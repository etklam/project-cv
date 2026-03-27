package me.hker.module.cv.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import me.hker.module.cv.entity.Cv
import org.apache.ibatis.annotations.Mapper

@Mapper
interface CvMapper : BaseMapper<Cv>
