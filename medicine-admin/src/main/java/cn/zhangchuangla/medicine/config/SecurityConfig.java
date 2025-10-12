package cn.zhangchuangla.medicine.config;

import cn.zhangchuangla.medicine.common.core.constants.SecurityConstants;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.security.filter.TokenAuthenticationFilter;
import cn.zhangchuangla.medicine.common.security.handel.AuthenticationEntryPointImpl;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Chuang
 */
@Configuration
public class SecurityConfig {

    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public SecurityConfig(AuthenticationEntryPointImpl authenticationEntryPoint,
                          @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TokenAuthenticationFilter tokenAuthenticationFilter) throws Exception {

        // 获取所有标记了@Anonymous注解的接口
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        Set<String> anonymousUrls = findAnonymousUrls(handlerMethods);

        return http
                // 跨域配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 统一异常处理：未认证和访问拒绝
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint))
                // 无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 关闭 CSRF、表单登录、Basic Auth
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 授权规则
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(SecurityConstants.WHITELIST).permitAll();
                    auth.requestMatchers(SecurityConstants.SWAGGER_WHITELIST).permitAll();
                    auth.requestMatchers(SecurityConstants.STATIC_RESOURCES_WHITELIST).permitAll();
                    auth.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll();
                    if (!anonymousUrls.isEmpty()) {
                        auth.requestMatchers(anonymousUrls.toArray(new String[0])).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                // 插入自定义 Token 认证过滤器
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 禁用 logout filter
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*", "Content-Type", "Authorization", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 查找所有标记了 @Anonymous 注解的接口URL
     *
     * @param handlerMethods 处理器方法映射
     * @return 包含所有匿名URL的集合
     */
    private Set<String> findAnonymousUrls(Map<RequestMappingInfo, HandlerMethod> handlerMethods) {
        Set<String> anonymousUrls = new HashSet<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            Anonymous methodAnonymous = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Anonymous.class);
            Anonymous classAnonymous = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Anonymous.class);

            if (methodAnonymous != null || classAnonymous != null) {
                Set<String> patterns = entry.getKey().getPatternValues();
                if (!patterns.isEmpty()) {
                    anonymousUrls.addAll(patterns);
                }
            }
        }
        return anonymousUrls;
    }

}
