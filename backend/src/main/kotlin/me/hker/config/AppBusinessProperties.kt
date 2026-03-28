package me.hker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppBusinessProperties(
    val credit: Credit = Credit(),
    val reward: Reward = Reward(),
    val upload: Upload = Upload(),
    val export: Export = Export(),
) {
    data class Credit(
        val signUpBonus: Int = 50,
        val createCvCost: Int = 10,
        val pdfExportCost: Int = 15,
    )

    data class Reward(
        val invite: Invite = Invite(),
    ) {
        data class Invite(
            val inviterBonus: Int = 20,
            val inviteeBonus: Int = 20,
        )
    }

    data class Upload(
        val path: String = "./data/uploads",
    )

    data class Export(
        val rendererBaseUrl: String = "http://localhost:3100",
        val frontendBaseUrl: String = "http://localhost:5173",
    )
}
