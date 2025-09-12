// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    id("org.sonarqube") version "6.3.1.5724"
}

sonar {
    properties {
        property("sonar.projectKey", "dijia1124_PlusPlusBattery")
        property("sonar.organization", "te08")
    }
}