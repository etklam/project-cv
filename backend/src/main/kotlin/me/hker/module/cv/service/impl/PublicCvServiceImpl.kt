package me.hker.module.cv.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.fasterxml.jackson.databind.ObjectMapper
import me.hker.common.ResourceNotFoundException
import me.hker.module.cv.dto.PublicCvDetailResponse
import me.hker.module.cv.dto.PublicCvDto
import me.hker.module.cv.dto.PublicCvSectionDto
import me.hker.module.cv.dto.PublicCvSummaryDto
import me.hker.module.cv.dto.PublicProfileResponse
import me.hker.module.cv.dto.PublicProfileUserDto
import me.hker.module.cv.entity.Cv
import me.hker.module.cv.entity.CvSection
import me.hker.module.cv.mapper.CvMapper
import me.hker.module.cv.mapper.CvSectionMapper
import me.hker.module.cv.service.PublicCvService
import me.hker.module.user.entity.User
import me.hker.module.user.service.UserService
import org.springframework.stereotype.Service

@Service
class PublicCvServiceImpl(
    private val userService: UserService,
    private val cvMapper: CvMapper,
    private val cvSectionMapper: CvSectionMapper,
    private val objectMapper: ObjectMapper,
) : PublicCvService {
    override fun getPublicProfile(username: String): PublicProfileResponse {
        val user = requirePublicUser(username)
        val userId = user.id ?: throw ResourceNotFoundException("public user not found")
        val publicCvs = cvMapper.selectList(
            QueryWrapper<Cv>()
                .eq("user_id", userId)
                .eq("is_public", true)
                .isNotNull("slug")
                .eq("is_deleted", false)
                .orderByDesc("updated_at"),
        )

        return PublicProfileResponse(
            user = user.toPublicProfileUserDto(),
            cvs = publicCvs.map { cv ->
                PublicCvSummaryDto(
                    id = cv.id ?: 0L,
                    title = cv.title,
                    slug = cv.slug.orEmpty(),
                    templateKey = cv.templateKey,
                    updatedAt = cv.updatedAt,
                )
            },
        )
    }

    override fun getPublicCv(username: String, slug: String): PublicCvDetailResponse {
        val user = requirePublicUser(username)
        val userId = user.id ?: throw ResourceNotFoundException("public user not found")
        val cv = cvMapper.selectOne(
            QueryWrapper<Cv>()
                .eq("user_id", userId)
                .eq("slug", slug)
                .eq("is_public", true)
                .eq("is_deleted", false)
                .last("LIMIT 1"),
        ) ?: throw ResourceNotFoundException("public cv not found")
        val sections = cvSectionMapper.selectList(
            QueryWrapper<CvSection>()
                .eq("cv_id", cv.id)
                .eq("is_deleted", false)
                .orderByAsc("sort_order", "id"),
        )

        return PublicCvDetailResponse(
            user = user.toPublicProfileUserDto(),
            cv = PublicCvDto(
                id = cv.id ?: 0L,
                title = cv.title,
                slug = cv.slug.orEmpty(),
                templateKey = cv.templateKey,
                updatedAt = cv.updatedAt,
            ),
            sections = sections.map { section ->
                PublicCvSectionDto(
                    id = section.id ?: 0L,
                    sectionType = section.sectionType,
                    sortOrder = section.sortOrder,
                    title = section.title,
                    content = objectMapper.readTree(section.content),
                )
            },
        )
    }

    private fun requirePublicUser(username: String): User =
        userService.findByUsername(username)
            ?: throw ResourceNotFoundException("public user not found")

    private fun User.toPublicProfileUserDto() = PublicProfileUserDto(
        username = username ?: throw ResourceNotFoundException("public user not found"),
        displayName = displayName,
        avatarPath = avatarPath,
    )
}
