package com.msa.bookmark

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication(scanBasePackages = ["com.msa.bookmark", "com.msa.common"])
@EnableDiscoveryClient
@EnableJpaAuditing
class BookmarkServiceApplication

fun main(args: Array<String>) {
    runApplication<BookmarkServiceApplication>(*args)
}
