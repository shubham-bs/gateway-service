package com.shubham.gatewayservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator routes(
            RouteLocatorBuilder builder,
            @Value("${routes.user-uri}") String userUri,
            @Value("${routes.expense-uri}") String expenseUri,
            @Value("${routes.sms-uri}") String smsUri,
            @Value("${routes.parser-uri}") String parserUri
    ) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/user/**")
                        .filters(f -> f.rewritePath("/user/(?<segment>.*)", "/${segment}"))
                        .uri(userUri))
                .route("expense-service", r -> r
                        .path("/expense/**")
                        .filters(f -> f.rewritePath("/expense/(?<segment>.*)", "/${segment}"))
                        .uri(expenseUri))
                .route("sms-service", r -> r
                        .path("/sms/**")
                        .filters(f -> f.rewritePath("/sms/(?<segment>.*)", "/${segment}"))
                        .uri(smsUri))
                .route("parser-service", r -> r
                        .path("/parser/**")
                        .filters(f -> f.rewritePath("/parser/(?<segment>.*)", "/${segment}"))
                        .uri(parserUri))
                .build();
    }
}
