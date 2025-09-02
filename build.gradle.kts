import com.diffplug.gradle.spotless.SpotlessApply
import nu.studer.gradle.jooq.JooqGenerate
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

buildscript {
    // TestContainer Postgresql needs to be on the classpath so that we can generate JOOQ code with Flyway on build.
    val postgresqlTestContainersDependency =
        libs.testcontainers.postgresql
            .get()
            .module
            .toString()
    val testContainersVersion = libs.versions.testcontainers.get()

    val flywayPostgresqlDependency =
        libs.flyway.postgres
            .get()
            .module
            .toString()
    val flywayPostgresqlVersion =
        libs.versions.flyway.postgresql
            .get()

    dependencies {
        classpath("$postgresqlTestContainersDependency:$testContainersVersion")
        classpath("$flywayPostgresqlDependency:$flywayPostgresqlVersion")
    }
}

plugins {
    // Kotlin
    alias(libs.plugins.kotlin.jvm)
    // Spring
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    // Kotlin Spring
    alias(libs.plugins.kotlin.spring)
    // Jooq
    alias(libs.plugins.jooq.gradle.plugin)
    // Flyway
    alias(libs.plugins.flyway.gradle.plugin)
    // Spotless
    alias(libs.plugins.spotless)
    // Google cloud tools jib
    alias(libs.plugins.google.cloud.tools.jib)
}

val groupId = "com.company.element"
val domainName = "divarak"
val jooqGeneratedSourcePackages = "$groupId.$domainName.infrastructure.persistence.postgres.jooq.generated"

group = groupId
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_21

val postgresqlDriver: String = "org.postgresql.Driver"
val postgresDockerImageName: DockerImageName = DockerImageName.parse("postgres:16")
val postgresqlContainer =
    PostgreSQLContainer<Nothing>(postgresDockerImageName).apply {
        withDatabaseName("divarak_service")
        withUsername("user")
        withPassword("password")
        start()
    }

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.jooq)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgres)
    implementation(libs.kotlin.reflect)
    implementation(libs.postgresql)

    implementation(libs.pinterest.ktlint)

    jooqGenerator(libs.postgresql)
    jooqGenerator(libs.jakarta.bind.api)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.bundles.testcontainers)
}

tasks.withType<KotlinCompile> {
    val javaVersion = libs.versions.java.get()
    val freeCompilerArguments = listOf("-Xjsr305=strict")

    compilerOptions {
        freeCompilerArgs.set(freeCompilerArguments)
        jvmTarget.set(JvmTarget.fromTarget(javaVersion))
    }

    finalizedBy(tasks.spotlessApply)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.STANDARD_ERROR, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

// not to generate plain jar file
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.build {
    dependsOn(tasks.withType<JooqGenerate>())
}

// add bootJar task, can be removed later if not needed
// tasks.withType<BootJar> {
//    archiveFileName.set("app.jar")
// }

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
        targetExclude(*targetExclusions)
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        ktlint(ktlintVersion)
        targetExclude(*targetExclusions)
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

flyway {
    url = postgresqlContainer.jdbcUrl
    driver = postgresqlDriver
    user = postgresqlContainer.username
    password = postgresqlContainer.password
    baselineOnMigrate = true
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}

jooq {
    version.set(libs.versions.jooq.codegen)
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

    configurations {
        create("main") {
            // name of the JOOQ configuration
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                logging = Logging.ERROR

                jdbc.apply {
                    driver = postgresqlDriver
                    url = postgresqlContainer.jdbcUrl + "/" + postgresqlContainer.databaseName
                    user = postgresqlContainer.username
                    password = postgresqlContainer.password

                    val sslProperty =
                        Property().apply {
                            key = "ssl"
                            value = "false"
                        }

                    properties.add(sslProperty)
                }

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"

                        forcedTypes.add(
                            org.jooq.meta.jaxb.ForcedType().apply {
                                name = "INSTANT"
                                types = "TIMESTAMPTZ"
                            },
                        )
                    }

                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isPojos = false
                        isImmutablePojos = false
                        isPojosAsKotlinDataClasses = false
                        isFluentSetters = true
                        isJavaTimeTypes = true
                        isKotlinNotNullRecordAttributes = true
                    }

                    target.apply {
                        packageName = jooqGeneratedSourcePackages
                    }
                }
            }
        }
    }
}

tasks.withType<JooqGenerate> {
    dependsOn(tasks.flywayMigrate)
    allInputsDeclared.set(true)

    // When Flyway + JOOQ Codegen have completed, stop the PostgreSQLContainer that's running for the build.
    doLast {
        postgresqlContainer.stop()
    }
}

jib {
    val imageTag = "v4"

    from {
        image = libs.versions.dockerBaseImage.get()
    }
    to {
        image = "${libs.versions.ecrRegistryUrl.get()}:$imageTag"
    }
    container {
        ports = listOf("8080")
        mainClass = "com.divarak.DivarakApplicationKt"
        jvmFlags = listOf("-Xms512m", "-Xmx512m")
    }
}

tasks.withType<SpotlessApply> {
    doLast {
        if (postgresqlContainer.isRunning) postgresqlContainer.stop()
    }
}
