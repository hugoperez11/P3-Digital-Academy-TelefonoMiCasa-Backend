package com.factoriaf5.telefono_micasa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/* import org.springframework.security.crypto.password.NoOpPasswordEncoder; */
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;

import com.factoriaf5.telefono_micasa.facades.encryptations.Base64Encoder;
import com.factoriaf5.telefono_micasa.services.JpaUserDetailsService;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    @Value("${api-endpoint}")
    String endpoint;

    MyBasicAuthenticationEntryPoint myBasicAuthenticationEntryPoint;

    JpaUserDetailsService jpaUserDetailsService;

    public SecurityConfig(JpaUserDetailsService jpaUserDetailsService,
            MyBasicAuthenticationEntryPoint basicEntryPoint) {
        this.jpaUserDetailsService = jpaUserDetailsService;
        this.myBasicAuthenticationEntryPoint = basicEntryPoint;

    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfiguration()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .logout(out -> out
                    .logoutUrl(endpoint + "/logout") 
                    .deleteCookies("JSESSIONID"))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers(HttpMethod.GET, endpoint + "/search/**").permitAll()
            .requestMatchers(HttpMethod.POST, endpoint + "/client").permitAll()
            .requestMatchers(HttpMethod.GET, endpoint + "/zone").permitAll()
            .requestMatchers(HttpMethod.POST, endpoint + "/personform").permitAll()
            .requestMatchers(HttpMethod.GET, endpoint + "/user/**" ).permitAll()
            .requestMatchers(HttpMethod.GET, endpoint + "/login").hasAnyRole("ADMIN","SALESMAN","USER")
            .requestMatchers(HttpMethod.POST, endpoint + "/appointments").authenticated()
            .requestMatchers(HttpMethod.GET, endpoint + "/appointments/user/**").hasAnyRole("SALESMAN", "USER")
            .requestMatchers(HttpMethod.POST, endpoint + "/salesmen").hasAnyRole("ADMIN")
            .requestMatchers(HttpMethod.GET, endpoint + "/salesmen").hasAnyRole("ADMIN", "SALESMAN")
            .requestMatchers(HttpMethod.DELETE, endpoint + "/salesmen").hasAnyRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, endpoint + "/zone/**").hasAnyRole("ADMIN")
            .requestMatchers(HttpMethod.POST, endpoint + "/property").hasAnyRole("SALESMAN")
            .requestMatchers(HttpMethod.PUT, endpoint + "/salesmen/update-password").hasAnyRole("SALESMAN")
            .anyRequest().authenticated())

                .userDetailsService(jpaUserDetailsService)
                .httpBasic(basic -> basic.authenticationEntryPoint(myBasicAuthenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        http.headers(header -> header.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    Base64Encoder base64Encoder() {
        return new Base64Encoder();
    }

    /*
     * @Bean
     * public PasswordEncoder passwordEncoder() {
     * return NoOpPasswordEncoder.getInstance(); // Desactiva el encriptado
     * }
     */

}