plugins {
    kotlin("plugin.serialization") version "2.0.21"
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

}

android {
    namespace = "com.example.percussionapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.percussionapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        /*
        externalNativeBuild {
            cmake {
                arguments("-DANDROID_STL=c++_shared")
                cppFlags += ""
            }
        }
         */
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++2a"
                arguments("-DANDROID_STL=c++_shared")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
        prefab = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    ndkVersion = "27.0.12077973"
    buildToolsVersion = "35.0.1"
    /*
       externalNativeBuild {
           cmake {
               path = file("CMakeLists.txt")
               version = "3.22.1"
           }
       }
       */
}

dependencies {
    //implementation(libs.androidx.material.icons.extended)
    implementation("com.google.oboe:oboe:1.9.3")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.google.oboe)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.kotlinx.serialization.json)
    //implementation "androidx.fragment:fragment-ktx:1.2.5"
    //implementation("com.patrykandpatrick.vico:compose:2.1.1")
    //implementation("org.tensorflow:tensorflow-lite:0.0.0-nightly-SNAPSHOT")
    //implementation("com.google.ai.edge.litert:litert-gpu:0.0.0-nightly-SNAPSHOT")
    //implementation("com.google.ai.edge.litert:litert-support:0.0.0-nightly-SNAPSHOT")
}