package com.gocomet.auction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI britishAuctionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("British Auction in RFQ System API")
                        .description("RESTful Backend APIs for British Auction-style bidding with dynamic trigger extensions and Forced Close ceiling enforcement.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GoComet Logistics Platform")
                                .url("https://www.gocomet.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
