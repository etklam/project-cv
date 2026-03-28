package me.hker.module.cv.dto

import com.fasterxml.jackson.databind.JsonNode

data class CvSectionPayload(
    val sectionType: String,
    val sortOrder: Int = 0,
    val title: String? = null,
    val content: JsonNode = com.fasterxml.jackson.databind.node.ObjectNode(com.fasterxml.jackson.databind.node.JsonNodeFactory.instance),
)
