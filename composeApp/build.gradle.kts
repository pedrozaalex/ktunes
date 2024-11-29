import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.buildConfig)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.coil)
            implementation(libs.coil.network.ktor)
            implementation(libs.multiplatformSettings)
            implementation(libs.multiplatformSettings.serialization)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kstore)
            implementation(libs.composeIcons.featherIcons)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.tab.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)
            implementation(libs.github.api)
            implementation(libs.jgit)
            implementation(libs.classgraph)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqlDelight.driver.sqlite)
        }

    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KTunes"
            packageVersion = "1.0.0"

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                bundleID = "com.soaresalex.ktunes.desktopApp"
            }
        }
    }
}

buildConfig {
    packageName("com.soaresalex.ktunes")

    buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${getSpotifyClientId()}\"")
    buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"${getSpotifyClientSecret()}\"")
    buildConfigField("String", "SPOTIFY_REDIRECT_HOST", "\"http://localhost\"")
    buildConfigField("String", "SPOTIFY_REDIRECT_PORT", "\"8080\"")
    buildConfigField("String", "SPOTIFY_REDIRECT_PATH", "\"/spotify-callback\"")
}

fun getSpotifyClientId(): String {
    return providers.gradleProperty("spotify.client.id")
        .orElse(providers.environmentVariable("SPOTIFY_CLIENT_ID"))
        .getOrElse("default_client_id")
}

fun getSpotifyClientSecret(): String {
    return providers.gradleProperty("spotify.client.secret")
        .orElse(providers.environmentVariable("SPOTIFY_CLIENT_SECRET"))
        .getOrElse("default_client_secret")
}

sqldelight {
    databases {
        create("MyDatabase") {
            // Database configuration here.
            // https://cashapp.github.io/sqldelight
            packageName.set("com.soaresalex.ktunes.db")
        }
    }
}
