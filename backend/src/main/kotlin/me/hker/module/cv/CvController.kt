package me.hker.module.cv

import me.hker.common.CurrentUserResolver
import me.hker.common.R
import me.hker.module.cv.dto.CreateCvRequest
import me.hker.module.cv.dto.CvDetailResponse
import me.hker.module.cv.dto.CvListResponse
import me.hker.module.cv.dto.SaveCvDraftRequest
import me.hker.module.cv.dto.UpdateCvSectionsRequest
import me.hker.module.cv.dto.UpdateCvRequest
import me.hker.module.cv.service.CvService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/me/cvs")
class CvController(
    private val cvService: CvService,
    private val currentUserResolver: CurrentUserResolver,
) {
    @GetMapping
    fun listCvs(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
    ): R<CvListResponse> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        return R.ok(CvListResponse(cvs = cvService.listByUser(resolvedUserId)))
    }

    @PostMapping
    fun createCv(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
        @RequestBody request: CreateCvRequest,
    ): R<CvDetailResponse> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        return R.ok(cvService.create(resolvedUserId, request))
    }

    @GetMapping("/{cvId}")
    fun getCv(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
        @PathVariable cvId: Long,
    ): R<CvDetailResponse> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        return R.ok(cvService.getById(resolvedUserId, cvId))
    }

    @PutMapping("/{cvId}")
    fun updateCv(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
        @PathVariable cvId: Long,
        @RequestBody request: UpdateCvRequest,
    ): R<CvDetailResponse> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        return R.ok(cvService.updateMetadata(resolvedUserId, cvId, request))
    }

    @PutMapping("/{cvId}/sections")
    fun updateSections(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
        @PathVariable cvId: Long,
        @RequestBody request: UpdateCvSectionsRequest,
    ): R<CvDetailResponse> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        return R.ok(cvService.updateSections(resolvedUserId, cvId, request))
    }

    @PutMapping("/{cvId}/draft")
    fun saveDraft(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
        @PathVariable cvId: Long,
        @RequestBody request: SaveCvDraftRequest,
    ): R<CvDetailResponse> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        return R.ok(cvService.saveDraft(resolvedUserId, cvId, request))
    }

    @DeleteMapping("/{cvId}")
    fun deleteCv(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
        @PathVariable cvId: Long,
    ): R<Nothing> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        cvService.delete(resolvedUserId, cvId)
        return R.ok()
    }
}
