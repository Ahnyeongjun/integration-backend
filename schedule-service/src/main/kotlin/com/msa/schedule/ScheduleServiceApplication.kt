package com.msa.schedule

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication(scanBasePackages = ["com.msa.schedule", "com.msa.common"])
@EnableDiscoveryClient
@EnableJpaAuditing
class ScheduleServiceApplication

fun main(args: Array<String>) {
    runApplication<ScheduleServiceApplication>(*args)
}
