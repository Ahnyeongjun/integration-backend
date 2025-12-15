package com.msa.festival

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.msa.festival", "com.msa.common"])
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
class FestivalServiceApplication

fun main(args: Array<String>) {
    runApplication<FestivalServiceApplication>(*args)
}
