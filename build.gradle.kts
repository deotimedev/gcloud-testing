import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.shadow)
}

group = "com.deotime"
version = "0.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.bundles.serialization)
    implementation(libs.bundles.ktor)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}

shadow {
    archivesName = "gcloud-instance-manager"
}