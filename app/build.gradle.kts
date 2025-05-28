plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.miempresa.totalhealth"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.miempresa.totalhealth"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
    // composeOptions { // Puedes descomentar y ajustar si tienes problemas con la versión del compilador de Compose
    //     kotlinCompilerExtensionVersion = "1.5.14" // Asegúrate que esta versión sea compatible con tu Kotlin y Compose BoM
    // }
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
    implementation(libs.androidx.material.icons.extended)

    // Firebase
    implementation(platform(libs.firebase.bom)) // Firebase Bill of Materials
    implementation(libs.firebase.auth.ktx)    // Firebase Authentication
    implementation(libs.firebase.analytics)   // Firebase Analytics
    implementation(libs.firebase.firestore.ktx) // Firebase Firestore
    implementation(libs.firebase.storage.ktx) // <--- DEPENDENCIA DE FIREBASE STORAGE AÑADIDA
    implementation(libs.firebase.appcheck.ktx) // <--- DEPENDENCIA DE FIREBASE APP CHECK KTX AÑADIDA
    implementation(libs.firebase.appcheck.debug) // <--- DEPENDENCIA DE FIREBASE APP CHECK DEBUG AÑADIDA

    // Coil para cargar imágenes
    implementation(libs.coil.compose) // <--- DEPENDENCIA DE COIL AÑADIDA

    // ViewModel y Navegación para Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // MPAndroidChart - Gráficos emocionales
    implementation(libs.mpandroidchart)

    // Dependencias de Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
