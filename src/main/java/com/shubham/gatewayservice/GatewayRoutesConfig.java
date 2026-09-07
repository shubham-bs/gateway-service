package com.shubham.gatewayservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest()
                        .getRemoteAddress()
                        .getAddress()
                        .getHostAddress()
        );
    }

    @Bean
    public RouteLocator routes(
            RouteLocatorBuilder builder,
            @Value("${routes.user-uri}") String userUri,
            @Value("${routes.expense-uri}") String expenseUri,
            @Value("${routes.sms-uri}") String smsUri,
            @Value("${routes.parser-uri}") String parserUri,
            KeyResolver ipKeyResolver
    ) {
        return builder.routes()

                // USER - REGISTER
                // 3 requests / 10 minutes / IP
                .route("user-register", r -> r
                        .path("/user/api/users/register")
                        .filters(f -> f
                                .rewritePath(
                                        "/user/(?<segment>.*)",
                                        "/${segment}"
                                )
                                .requestRateLimiter()
                                .rateLimiter(
                                        RedisRateLimiter.class,
                                        config -> config
                                                .setReplenishRate(1)
                                                .setBurstCapacity(600)
                                                .setRequestedTokens(200)
                                )
                                .configure(config -> config
                                        .setKeyResolver(ipKeyResolver)
                                )
                        )
                        .uri(userUri)
                )

                // USER - LOGIN
                // 5 requests / minute / IP
                .route("user-login", r -> r
                        .path("/user/api/users/login")
                        .filters(f -> f
                                .rewritePath(
                                        "/user/(?<segment>.*)",
                                        "/${segment}"
                                )
                                .requestRateLimiter()
                                .rateLimiter(
                                        RedisRateLimiter.class,
                                        config -> config
                                                .setReplenishRate(1)
                                                .setBurstCapacity(60)
                                                .setRequestedTokens(12)
                                )
                                .configure(config -> config
                                        .setKeyResolver(ipKeyResolver)
                                )
                        )
                        .uri(userUri)
                )

                // USER - GENERAL API
                // 100 requests / minute / IP
                .route("user-service", r -> r
                        .path("/user/**")
                        .filters(f -> f
                                .rewritePath(
                                        "/user/(?<segment>.*)",
                                        "/${segment}"
                                )
                                .requestRateLimiter()
                                .rateLimiter(
                                        RedisRateLimiter.class,
                                        config -> config
                                                .setReplenishRate(10)
                                                .setBurstCapacity(600)
                                                .setRequestedTokens(6)
                                )
                                .configure(config -> config
                                        .setKeyResolver(ipKeyResolver)
                                )
                        )
                        .uri(userUri)
                )

                // EXPENSE SERVICE
                // 100 requests / minute / IP
                .route("expense-service", r -> r
                        .path("/expense/**")
                        .filters(f -> f
                                .rewritePath(
                                        "/expense/(?<segment>.*)",
                                        "/${segment}"
                                )
                                .requestRateLimiter()
                                .rateLimiter(
                                        RedisRateLimiter.class,
                                        config -> config
                                                .setReplenishRate(10)
                                                .setBurstCapacity(600)
                                                .setRequestedTokens(6)
                                )
                                .configure(config -> config
                                        .setKeyResolver(ipKeyResolver)
                                )
                        )
                        .uri(expenseUri)
                )

                // SMS - POST
                // 20 requests / minute / IP
                .route("sms-post", r -> r
                        .path("/sms/api/sms")
                        .and()
                        .method("POST")
                        .filters(f -> f
                                .rewritePath(
                                        "/sms/(?<segment>.*)",
                                        "/${segment}"
                                )
                                .requestRateLimiter()
                                .rateLimiter(
                                        RedisRateLimiter.class,
                                        config -> config
                                                .setReplenishRate(1)
                                                .setBurstCapacity(60)
                                                .setRequestedTokens(3)
                                )
                                .configure(config -> config
                                        .setKeyResolver(ipKeyResolver)
                                )
                        )
                        .uri(smsUri)
                )

                // SMS - OTHER ENDPOINTS
                // 100 requests / minute / IP
                .route("sms-service-general", r -> r
                        .path("/sms/**")
                        .filters(f -> f
                                .rewritePath(
                                        "/sms/(?<segment>.*)",
                                        "/${segment}"
                                )
                                .requestRateLimiter()
                                .rateLimiter(
                                        RedisRateLimiter.class,
                                        config -> config
                                                .setReplenishRate(10)
                                                .setBurstCapacity(600)
                                                .setRequestedTokens(6)
                                )
                                .configure(config -> config
                                        .setKeyResolver(ipKeyResolver)
                                )
                        )
                        .uri(smsUri)
                )

                // PARSER SERVICE
                // 100 requests / minute / IP
                .route("parser-service", r -> r
                        .path("/parser/**")
                        .filters(f -> f
                                .rewritePath(
                                        "/parser/(?<segment>.*)",
                                        "/${segment}"
                                )
                                .requestRateLimiter()
                                .rateLimiter(
                                        RedisRateLimiter.class,
                                        config -> config
                                                .setReplenishRate(10)
                                                .setBurstCapacity(600)
                                                .setRequestedTokens(6)
                                )
                                .configure(config -> config
                                        .setKeyResolver(ipKeyResolver)
                                )
                        )
                        .uri(parserUri)
                )

                .build();
    }
}