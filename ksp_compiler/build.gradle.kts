plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
//    kotlin("kapt")
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":ksp_annotation"))
    implementation(libs.symbol.processing.api)//引入ksp
//    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
//    implementation("com.google.auto.service:auto-service:1.1.1")
//    kapt("com.google.auto.service:auto-service:1.1.1")
}