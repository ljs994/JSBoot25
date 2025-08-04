package org.mbc.board.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mbc.board.security.CustomUserDetailsService;
import org.mbc.board.security.handler.Custom403Handler;
import org.mbc.board.security.handler.CustomSocialLoginSuccessHandler;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Log4j2
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)

public class CustomSecurityConfig {

    private final DataSource dataSource;  
    
    private final CustomUserDetailsService customUserDetailsService;


    @Bean
    public SecurityFilterChain FilterChain(HttpSecurity http) throws Exception {

        log.info("----------------CustomSecurityConfig.filterChain() 메서드 실행 ----------------------");
        log.info("--------------강제로 로그인 하지 않음--------------");
        log.info("--------------모든 사용자가 모든 경로에 접근 할 수 있음.---------");
        log.info("--------------application.properties파일에 로그 출력 레벨 추가---------");

            http.formLogin(form -> {
                log.info("======= 커스텀한 로그인 페이지 호출=======");
                form.loginPage("/member/login");  // 로그인페이지 커스텀 p694
            });


            http.csrf(httpSecurityCsrfConfigurer -> {

                log.info("======= CSRF 비활성화 호출=======");
                httpSecurityCsrfConfigurer.disable();
            });

            http.rememberMe(httpSecurityRememberMeConfigurer -> {
                httpSecurityRememberMeConfigurer.key("12345678")
                        .tokenRepository(persistentTokenRepository())
                        .userDetailsService(customUserDetailsService)
                        .tokenValiditySeconds(60*60*24*30);
                log.info("======= 자동 로그인기법 rememberMe 호출=======");
            });

            http.oauth2Login(httpSecurityOAuth2LoginConfigurer -> {
                httpSecurityOAuth2LoginConfigurer.loginPage("/member/login");
                httpSecurityOAuth2LoginConfigurer.successHandler(authenticationSuccessHandler());
            });

            http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
                httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler());
            });

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new Custom403Handler();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        log.info("======= persistentTokenRepository 토큰생성기법 호출 =======");
        return jdbcTokenRepository;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("--- CustomSecurityConfig.WebSecurityCustomizer() 메서드 실행 ---------");
        log.info("--- toStaticResources에 ignoring처리됨 ---");
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("======= 패스워드 암화기법 처리 메서드 실행 =======");
        return new BCryptPasswordEncoder(); // 해시코드로 암호화기법을 적용
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {

        return new CustomSocialLoginSuccessHandler(passwordEncoder());

    }

}
