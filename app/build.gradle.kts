plugins {
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutLibraries)
}

android {
    namespace = "de.readeckapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.readeckapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 100
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            val appKeystoreFile = System.getenv()["KEYSTORE"] ?: "none"
            val appKeyAlias = System.getenv()["KEY_ALIAS"]
            val appKeystorePassword = System.getenv()["KEYSTORE_PASSWORD"]
            val appKeyPassword = System.getenv()["KEY_PASSWORD"]

            keyAlias = appKeyAlias
            storeFile = file(appKeystoreFile)
            storePassword = appKeystorePassword
            keyPassword = appKeyPassword
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = true
        }
        applicationVariants.all {
            outputs.all {
                if (outputFile != null && (outputFile.name.endsWith(".apk") || outputFile.name.endsWith(".aab"))) {
                    val extension = if (outputFile.name.endsWith(".apk")) "apk" else "aab"
                    val newName = "ReadeckApp-${versionName}.${extension}"
                    (this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl)?.outputFileName = newName
                }
            }
        }
    }
    flavorDimensions += "version"
    productFlavors {
        create("githubSnapshot") {
            dimension = "version"
            applicationIdSuffix = ".snapshot"
            versionName = System.getenv()["SNAPSHOT_VERSION_NAME"] ?: "${defaultConfig.versionName}-snapshot"
            versionCode = System.getenv()["SNAPSHOT_VERSION_CODE"]?.toInt() ?: defaultConfig.versionCode
            signingConfig = signingConfigs.getByName("release")
        }
        create("githubRelease") {
            dimension = "version"
            versionName = System.getenv()["RELEASE_VERSION_NAME"] ?: defaultConfig.versionName
            versionCode = System.getenv()["RELEASE_VERSION_CODE"]?.toInt() ?: defaultConfig.versionCode
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.ui.test.junit4.android)
    // hilt
    ksp(libs.dagger.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation)
    implementation(libs.dagger.hilt.android)
    testImplementation(libs.dagger.hilt.android.testing)
    kspTest(libs.androidx.hilt.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.timber)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlinx.datetime)
    ksp(libs.androidx.room.compiler)
    kapt(libs.retrofit.response.type.keeper)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)
    implementation(libs.okhttp3.logging.interceptor)
    testImplementation(libs.okhttp3.mockserver)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.security.crypto)
    implementation(libs.google.crypto.tink)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
}

aboutLibraries {
    configPath = "config"
}
