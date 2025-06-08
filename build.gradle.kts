plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spotless)
}

group = "com.divarak"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.jooq)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgres)
    implementation(libs.kotlin.reflect)
    implementation(libs.postgresql)

    implementation(libs.pinterest.ktlint)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    val ktlintVersion =
        libs.versions.pinterest.ktlint
            .get()

    val targetExclusions =
        arrayOf(
            "build/**/*",
            ".gradle/**/*",
            ".idea/**/*",
        )

    isEnforceCheck = true

    kotlin {
        ktlint(ktlintVersion)
        targetExclude(targetExclusions)
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        ktlint(ktlintVersion)
        targetExclude(targetExclusions)
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("miscellaneous") {
        target(
            "**/.json",
            "**.*.graphql*",
            "**/.gitignore",
            "**/.gitignore",
            "**/*.properties",
            "**/*.md",
            "**/*.xml",
            "**/*.yaml",
            "**/*.yml",
        )
        targetExclude(*targetExclusions)
        trimTrailingWhitespace()
        endWithNewline()
    }
}
