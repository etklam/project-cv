package me.hker.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class PublicCvPostgresIntegrationTest : PostgresIntegrationTestSupport() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `fresh postgres database should bootstrap flyway schema and public profile endpoint`() {
        val appliedScripts = jdbcTemplate.queryForList(
            """
            SELECT script
            FROM flyway_schema_history
            WHERE success = true
              AND version IS NOT NULL
            ORDER BY installed_rank
            """.trimIndent(),
            String::class.java,
        )

        assertEquals("B11__baseline_schema.sql", appliedScripts.first())
        assertTrue(
            appliedScripts.contains("V12__add_user_role.sql"),
            "Fresh databases should bootstrap from the baseline and still apply later migrations",
        )

        mockMvc.get("/api/v1/public/alice@example.com")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.user.email") { value("alice@example.com") }
                jsonPath("$.data.cvs[0].slug") { value("product-resume") }
            }
    }

    @Test
    fun `fresh postgres database should expose seeded public cv detail sections`() {
        mockMvc.get("/api/v1/public/alice@example.com/product-resume")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.cv.templateKey") { value("minimal") }
                jsonPath("$.data.sections[0].sectionType") { value("summary") }
                jsonPath("$.data.sections[1].sectionType") { value("experience") }
                jsonPath("$.data.sections[2].sectionType") { value("skills") }
            }
    }
}
