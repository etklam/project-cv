package me.hker.module.user.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import me.hker.module.user.entity.User
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserMapper : BaseMapper<User>
