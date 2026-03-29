package me.hker.config

import jakarta.servlet.http.HttpServletResponse
import me.hker.module.auth.JwtAuthFilter
import org.springframework.http.MediaType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/health").permitAll()
                it.requestMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/logout",
                    "/api/v1/public/**",
                    "/api/v1/templates",
                    "/api/v1/export/print/**",
                ).permitAll()
                it.requestMatchers(
                    "/api/v1/auth/me",
                    "/api/v1/auth/change-locale",
                    "/api/v1/me/**",
                ).authenticated()
                it.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                it.anyRequest().authenticated()
            }
            .httpBasic(Customizer.withDefaults())
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.writer.write("""{"code":401,"message":"UNAUTHORIZED","data":null}""")
                }
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
