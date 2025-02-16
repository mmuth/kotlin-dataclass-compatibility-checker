import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJVM)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotest)
    alias(libs.plugins.shadowJar)
    application
}

repositories {
    mavenCentral()
}

val patchVersion = System.getenv("GITHUB_RUN_NUMBER") ?: "0-SNAPSHOT"
group = "de.mmuth"
version = "0.2.$patchVersion"

dependencies {
    implementation(libs.clikt)
    implementation(libs.bundles.logback)
    implementation(libs.slf4j.api)
    implementation(libs.kotlin.compiler)
    implementation(libs.kotlin.reflect)
    implementation(libs.mockk)
    testImplementation(libs.bundles.kotest)
}

val javaVersion = JavaVersion.VERSION_17

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.majorVersion))
        sourceSets {
            main {
                kotlin.srcDir(layout.buildDirectory.dir("generated/src/main/kotlin"))
            }
        }
    }
}

application {
    mainClass.set("de.mmuth.KotlinDataClassCompatibilityCheckerKt")
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    manifest.attributes["Main-Class"] = "de.mmuth.KotlinDataClassCompatibilityCheckerKt"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}

detekt {
    buildUponDefaultConfig = true
    config.from("$rootDir/detekt-overrides.yml")
}

