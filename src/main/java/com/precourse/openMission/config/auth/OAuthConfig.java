package com.precourse.openMission.config.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;

@Configuration
public class OAuthConfig {
    @Bean
    public OAuth2UserService oAuth2UserService() {
        return new DefaultOAuth2UserService();
    }
}
