package mx.aplazo.microservices.wm.account;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Disables Spring Security for all endpoints when the {@code local} profile is active.
 * <p>
 * Uses {@link WebSecurityCustomizer} to bypass all security filter chains at the web layer,
 * avoiding the Spring Security 6 constraint that prohibits two filter chains from matching
 * "any request" simultaneously.
 */
@Configuration
@Profile("local")
public class NoSecurityConfig {

    @Bean
    public WebSecurityCustomizer localSecurityIgnore() {
        return web -> web.ignoring().requestMatchers(new AntPathRequestMatcher("/**"));
    }
}
