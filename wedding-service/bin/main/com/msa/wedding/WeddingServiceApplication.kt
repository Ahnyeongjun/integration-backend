package com.msa.wedding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication(scanBasePackages = ["com.msa.wedding", "com.msa.common"])
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
class WeddingServiceApplication

fun main(args: Array<String>) {
    runApplication<WeddingServiceApplication>(*args)
}
