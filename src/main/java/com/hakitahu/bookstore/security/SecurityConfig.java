package com.hakitahu.bookstore.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomOAuth2UserService customOAuth2UserService,
			OAuth2SuccessHandler oAuth2SuccessHandler) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.customOAuth2UserService = customOAuth2UserService;
		this.oAuth2SuccessHandler = oAuth2SuccessHandler;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/api/auth/**").permitAll()
	            .requestMatchers("/oauth2/**").permitAll()
	            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories/**").permitAll()
	            .requestMatchers("/api/categories", "/api/categories/**").hasAuthority("admin")
	            .anyRequest().authenticated()
	        )
	        .oauth2Login(oauth2 -> oauth2
	            .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
	            .successHandler(oAuth2SuccessHandler)
	        )
	        // THÊM KHỐI NÀY VÀO ĐÂY: Xử lý lỗi cho các API
	        .exceptionHandling(exception -> exception
	        	    // Bắt lỗi 401 (Chưa có Token hoặc Token sai)
	        	    .authenticationEntryPoint((request, response, authException) -> {
	        	        if (request.getRequestURI().startsWith("/api/")) {
	        	            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Sai Token hoặc chưa đăng nhập");
	        	        } else {
	        	            response.sendRedirect("/oauth2/authorization/google"); 
	        	        }
	        	    })
	        	    // BẮT THÊM LỖI 403 (Có Token nhưng không phải Admin)
	        	    .accessDeniedHandler((request, response, accessDeniedException) -> {
	        	        if (request.getRequestURI().startsWith("/api/")) {
	        	            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền Admin");
	        	        }
	        	    })
	        	);

	    http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}