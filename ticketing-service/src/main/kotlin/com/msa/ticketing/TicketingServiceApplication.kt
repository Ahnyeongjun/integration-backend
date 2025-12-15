package com.msa.ticketing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.msa.ticketing", "com.msa.common"])
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
class TicketingServiceApplication

fun main(args: Array<String>) {
    runApplication<TicketingServiceApplication>(*args)
}
