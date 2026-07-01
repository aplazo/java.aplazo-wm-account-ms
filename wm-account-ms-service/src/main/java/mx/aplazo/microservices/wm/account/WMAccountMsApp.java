package mx.aplazo.microservices.wm.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;
import mx.aplazo.annotation.EnableAplazoCloudAwsSqs;
import mx.aplazo.annotation.EnableAplazoFeingClientInterceptor;
import mx.aplazo.annotation.EnableAplazoFilterModule;
import mx.aplazo.annotation.EnableErrorHandlerModule;
import mx.aplazo.security.annotation.EnableSecurityModule;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;


/**
 * @author Aplazo
 */
@SpringBootApplication
@EnableAplazoFilterModule
@EnableErrorHandlerModule
@EnableSecurityModule
@EnableAplazoFeingClientInterceptor
@EnableAplazoCloudAwsSqs
@EnableFeignClients(basePackages = "mx.aplazo.microservices.wm.account.feign")
@Slf4j
public class WMAccountMsApp {
	/**
	 * Main method of WMAccountMs.
	 * @param args type of String[]
	 */
	public static void main(String[] args) {
		SpringApplication.run(WMAccountMsApp.class, args);
		log.info("WMAccountMsApp start ok!");
	}

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/v3/api-docs.yaml", "/v3/api-docs.yml");
    }
}
