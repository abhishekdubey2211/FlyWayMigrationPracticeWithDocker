//package com.security.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
//import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
//import org.springframework.security.web.session.HttpSessionEventPublisher;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration {
//
//    // ✅ Detect if running in development mode
//    private boolean isDevelopmentMode() {
//        return true;
////                "true".equals(System.getProperty("devMode", "false"));
//    }
//    // ✅ Password Encoder
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    // ✅ In-Memory User Storage
//    @Bean
//    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
//        UserDetails user = User.builder()
//                .username("user")
//                .password(passwordEncoder.encode("user123"))
//                .roles("USER")
//                .build();
//
//        UserDetails admin = User.builder()
//                .username("admin")
//                .password(passwordEncoder.encode("admin123"))
//                .roles("ADMIN")
//                .build();
//
//        return new InMemoryUserDetailsManager(user, admin);
//    }
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // ✅ Enforce HTTPS (Strict Transport Security)
//                .requiresChannel(channel -> {
//                    if (!isDevelopmentMode()) {
//                        channel.anyRequest().requiresSecure();
//                    }
//                })
//                // ✅ Enable CSRF protection with secure cookies
//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // More secure
//                )
//
//                // ✅ Enable CORS
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//
//                // ✅ Apply security headers
//                .headers(headers -> headers
//                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
//                        .contentSecurityPolicy(csp -> csp.policyDirectives(
//                                "default-src 'self'; script-src 'self' 'unsafe-inline' https://maxcdn.bootstrapcdn.com;" +
//                                        "style-src 'self' 'unsafe-inline' https://maxcdn.bootstrapcdn.com https://getbootstrap.com;" +
//                                        "img-src 'self' data:; font-src 'self'; object-src 'none'; form-action 'self'"
//                        ))
//                        .frameOptions(frameOptions -> frameOptions.deny()) // Prevent Clickjacking
//                        .httpStrictTransportSecurity(hsts -> hsts
//                                .includeSubDomains(true)
//                                .maxAgeInSeconds(31536000) // Enforce HTTPS for 1 year
//                        )
//                )
//
//                // ✅ Secure APIs
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/auth/**").permitAll() // Allow authentication requests
////                        .requestMatchers("/admin/**").hasRole("ADMIN") // Restrict admin APIs
//                        .anyRequest().authenticated()
//                )
//
//                // ✅ Enable form login & logout
//                .formLogin(Customizer.withDefaults())
//                .logout(Customizer.withDefaults());
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsFilter corsFilter() {
//        return new CorsFilter(corsConfigurationSource());
//    }
//
//    @Bean
//    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:8080")); // ✅ Corrected
//        config.setAllowedMethods(List.of("GET", "POST")); // Only allow necessary methods
//        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
//
//    // ✅ Prevent Session Hijacking
//    @Bean
//    public HttpSessionEventPublisher httpSessionEventPublisher() {
//        return new HttpSessionEventPublisher();
//    }
//}
