plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    kotlin("plugin.jpa") version "2.1.0"
    id("maven-publish")
}

group = "com.msa"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.0")
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.0")
    implementation("org.springframework.boot:spring-boot-starter-security:3.4.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")
    implementation(kotlin("reflect"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.msa"
            artifactId = "common-lib"
            version = "1.0.0"
            from(components["java"])
        }
    }
    repositories {
        // Local Maven for development
        mavenLocal()

        // Nexus/Artifactory for production
        // maven {
        //     url = uri("https://your-nexus-server/repository/maven-releases/")
        //     credentials {
        //         username = project.findProperty("nexusUsername") as String? ?: ""
        //         password = project.findProperty("nexusPassword") as String? ?: ""
        //     }
        // }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}
