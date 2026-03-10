// ============================================================
// build.gradle.kts (app) - SLIOR Mobile App
// Kotlin 2.0.21 · Android API 24+ · MVVM + Clean Architecture + Jetpack Compose
// ============================================================
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.slior"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.slior"
        minSdk = 24          // Android 7.0 Nougat (cubre ~95% de dispositivos)
        targetSdk = 34       // Android 14
        versionCode = 1
        versionName = "1.0.0"

    testInstrumentationRunner = "com.slior.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // En release usar la URL del túnel (sustituir por la URL real de Cloudflare)
            buildConfigField("String", "BASE_URL", "\"https://CHANGE-ME.trycloudflare.com/\"")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            // Emulador local → 10.0.2.2 apunta al localhost del PC
            // Para usar el túnel: cambiar por "https://tu-url.trycloudflare.com/"
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true   // Activa Jetpack Compose
        buildConfig = true   // Activa BuildConfig para BASE_URL configurable
    }
}

dependencies {

    // ======= Android Core =======
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.2")

    // ======= Jetpack Compose BOM (gestiona versiones de todas las librerías Compose) =======
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ======= Navigation Compose =======
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ======= Architecture Components (ViewModel + Lifecycle) =======
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // ======= Room Database (persistencia local offline-first) =======
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ======= Retrofit + OkHttp (cliente HTTP) =======
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ======= Hilt (inyección de dependencias) =======
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")

    // ======= Coroutines =======
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ======= DataStore (reemplaza SharedPreferences para token JWT) =======
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // ======= WorkManager (sincronización en background) =======
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // ======= Mapas: OSMDroid (OpenStreetMap, sin clave API) =======
    implementation("org.osmdroid:osmdroid-android:6.1.17")

    // ======= Geolocalización =======
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // ======= Escáner de Códigos de Barras: ZXing =======
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // ======= Gson (serialización JSON) =======
    implementation("com.google.code.gson:gson:2.10.1")

    // ======= Testing =======
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
}
