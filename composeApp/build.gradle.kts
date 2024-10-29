import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.0.20"
}

repositories {
    google {
        mavenContent {
            includeGroupAndSubgroups("androidx")
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
        }
    }
    mavenCentral()
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation("androidx.compose.material:material-icons-extended:1.7.4")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("com.charleskorn.kaml:kaml:0.61.0")
        }
        desktopTest.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test-junit5")
            implementation(compose.desktop.currentOs)
        }
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
//        implementation(libs.kotlin.test)
        implementation("io.kotest:kotest-runner-junit5:5.9.1")
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        implementation(compose.uiTest)
    }
}

compose.desktop {
    application {
        mainClass = "com.thecryingbeard.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "multipack"
            packageVersion = "1.0.0"

            macOS {
                iconFile.set(project.file("crying_beard_icon.icns"))
            }
            windows {
                iconFile.set(project.file("crying_beard_icon.ico"))
            }
            linux {
                iconFile.set(project.file("crying_beard_icon-128x128.png"))
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
 }
