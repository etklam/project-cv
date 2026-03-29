package me.hker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppBusinessProperties(
    val credit: Credit = Credit(),
    val reward: Reward = Reward(),
    val upload: Upload = Upload(),
    val export: Export = Export(),
    val auth: Auth = Auth(),
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

    data class Auth(
        val cookie: Cookie = Cookie(),
    ) {
        data class Cookie(
            val secure: Boolean = false,
            val sameSite: String = "Lax",
            val path: String = "/api",
            val maxAgeDays: Long = 7,
        )
    }
}
