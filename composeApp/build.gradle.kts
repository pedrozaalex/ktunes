import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	alias(libs.plugins.multiplatform)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.compose)
	alias(libs.plugins.kotlinx.serialization)
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
			implementation(libs.ktor.client.cio)
			implementation(libs.ktor.client.auth)
			implementation(libs.ktor.client.content.negotiation)
			implementation(libs.ktor.client.serialization)
			implementation(libs.ktor.client.logging)
			implementation(libs.ktor.serialization.kotlinx.json)
			implementation(libs.kotlinx.serialization.json)
			implementation(libs.koin.core)
			implementation(libs.koin.compose)
			implementation(libs.coil)
			implementation(libs.multiplatformSettings)
			implementation(libs.multiplatformSettings.serialization)
			implementation(libs.composeIcons.featherIcons)
			implementation(libs.voyager.navigator)
			implementation(libs.voyager.screenmodel)
			implementation(libs.voyager.tab.navigator)
			implementation(libs.voyager.transitions)
			implementation(libs.voyager.koin)
			implementation(libs.lifecycle.viewmodel.compose)
			implementation(libs.jaudiotagger)
			implementation("net.java.dev.jna:jna:5.10.0")
			implementation("net.java.dev.jna:jna-platform:5.10.0")
			implementation("org.freedesktop.gstreamer:gst1-java-core:1.4.0")
		}

		commonTest.dependencies {
			implementation(kotlin("test"))
			@OptIn(ExperimentalComposeLibrary::class) implementation(compose.uiTest)
			implementation(libs.kotlinx.coroutines.test)
		}

		jvmMain.dependencies {
			implementation(compose.desktop.currentOs)
			implementation(libs.kotlinx.coroutines.swing)
		}

	}
}

compose.desktop {
	application {
		mainClass = "MainKt"

		buildTypes.release.proguard {
			version.set("7.4.0")
			configurationFiles.from("compose-desktop.pro")
		}

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
