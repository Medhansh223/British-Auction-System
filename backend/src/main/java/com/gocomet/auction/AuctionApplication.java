package com.gocomet.auction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class AuctionApplication {

    public static void main(String[] args) {
        log.info("Starting British Auction RFQ Application...");
        SpringApplication.run(AuctionApplication.class, args);
        log.info("British Auction RFQ Application started successfully! Ready to accept requests.");
    }
}
