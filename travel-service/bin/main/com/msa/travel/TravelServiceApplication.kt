package com.msa.travel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication(scanBasePackages = ["com.msa.travel", "com.msa.common"])
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
class TravelServiceApplication

fun main(args: Array<String>) {
    runApplication<TravelServiceApplication>(*args)
}
