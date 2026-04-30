import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.spotless)
    alias(libs.plugins.versions)
    checkstyle
    jacoco
}

group = "com.example"
version = providers.gradleProperty("projectVersion").get()

repositories {
    mavenCentral()
}

val projectVersion = version.toString()
val applicationMainClass = "com.example.javastarterboilerplate.Application"
val spotlessIgnorePatterns =
    file(".spotlessignore")
        .takeIf { it.exists() }
        ?.readLines()
        ?.map(String::trim)
        ?.filter { it.isNotBlank() && !it.startsWith("#") }
        ?: emptyList()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

micronaut {
    version(
        libs.versions.micronaut.platform
            .get(),
    )
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.example.javastarterboilerplate.*")
    }
}

application {
    mainClass.set(applicationMainClass)
}

dependencies {
    annotationProcessor("io.micronaut.data:micronaut-data-processor")
    annotationProcessor("io.micronaut.openapi:micronaut-openapi")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")

    implementation(platform("io.micronaut.platform:micronaut-platform:${libs.versions.micronaut.platform.get()}"))
    implementation(platform(libs.aws.sdk.bom))
    implementation(platform(libs.dss.bom))

    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("io.micronaut.data:micronaut-data-hibernate-jpa")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")

    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:url-connection-client")

    implementation(libs.pdfbox)
    implementation("eu.europa.ec.joinup.sd-dss:dss-document")
    implementation("eu.europa.ec.joinup.sd-dss:dss-pades-pdfbox")

    implementation(libs.logstash.logback.encoder)
    implementation(libs.jackson.dataformat.yaml)

    compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")

    runtimeOnly("org.yaml:snakeyaml")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly(libs.h2)
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")
    runtimeOnly(libs.janino)

    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation(libs.assertj)
    testImplementation(libs.archunit.junit5)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

spotless {
    java {
        target("src/**/*.java")
        targetExclude(*spotlessIgnorePatterns.toTypedArray())
        googleJavaFormat(
            libs.versions.google.java.format
                .get(),
        )
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts", "gradle/**/*.kts")
        targetExclude(*spotlessIgnorePatterns.toTypedArray())
        ktlint()
    }
    format("misc") {
        target(
            "*.md",
            "*.yaml",
            "*.properties",
            ".gitignore",
            ".gitattributes",
            ".spotlessignore",
            ".editorconfig",
            ".dockerignore",
            ".env.example",
            ".github/**/*.yaml",
            "Makefile",
            "docker-compose.yaml",
            "docs/**/*.md",
            "k8s/**/*.yaml",
            "src/**/*.yaml",
        )
        targetExclude(*spotlessIgnorePatterns.toTypedArray())
        trimTrailingWhitespace()
        endWithNewline()
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("application*.yaml") {
        filter { line: String ->
            line.replace("@projectVersion@", projectVersion)
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.isFork = true
    options.forkOptions.jvmArgs =
        mutableListOf(
            "-Dmicronaut.openapi.expand.api.version=$projectVersion",
            "-Dmicronaut.openapi.expand.api.title=java-starter-boilerplate",
            "-Dmicronaut.openapi.expand.api.description=Production-grade Micronaut starter for future document sealing and signing services",
        )
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
    testLogging {
        events("failed", "skipped")
    }
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude("com/example/javastarterboilerplate/Application.class")
                }
            },
        ),
    )
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.test)

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude("com/example/javastarterboilerplate/Application.class")
                }
            },
        ),
    )

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "1.0".toBigDecimal()
            }
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    systemProperty("app.version", projectVersion)
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            "Main-Class" to applicationMainClass,
            "Implementation-Title" to rootProject.name,
            "Implementation-Version" to projectVersion,
        )
    }
}

val fatJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Builds an executable fat JAR for CLI and service execution."
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to applicationMainClass,
            "Implementation-Title" to rootProject.name,
            "Implementation-Version" to projectVersion,
        )
    }

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath
            .get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
    )
}

tasks.assemble {
    dependsOn(fatJar)
}

tasks.check {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
    dependsOn(tasks.named("spotlessCheck"))
}
