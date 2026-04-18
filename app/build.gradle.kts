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

    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    defaultConfig {
        multiDexEnabled = true
        applicationId = "de.readeckapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1000
        versionName = "0.10.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "$applicationId"
        buildConfigField("String", "APP_URL", "\"https://github.com/jensomato/ReadeckApp\"")
        buildConfigField("String", "APP_NAME", "\"Readeck App\"")
        buildConfigField("String", "APP_LOGO_BASE64", "\"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\""
        )

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas".toString()
            }
        }
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
            isMinifyEnabled = false
            isShrinkResources = false
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
    sourceSets {
        getByName("debug").assets.srcDirs(files("$projectDir/schemas"))
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
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
    implementation(libs.androidx.browser)
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
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)
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
    implementation(libs.treessence)
    implementation(libs.accompanist.permissions)
    implementation(libs.appauth)
}

aboutLibraries {
    registerAndroidTasks = false
    prettyPrint = true
    configPath = "config"
}

tasks.whenTaskAdded {
    if (name.contains("ArtProfile")) {
        enabled = false
    }
}
