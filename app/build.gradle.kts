plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- Додаємо нові залежності ---

    // Room (База даних)
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version") // Якщо використовуєте kapt
    // annotationProcessor("androidx.room:room-compiler:$room_version") // Для Java
    // ksp("androidx.room:room-compiler:$room_version") // Якщо використовується KSP
    implementation("androidx.room:room-ktx:$room_version") // Підтримка Kotlin Coroutines для Room

    // Lifecycle (ViewModel & LiveData)
    val lifecycle_version = "2.8.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")

    // Coroutines (Асинхронність)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Navigation Component (Навігація між фрагментами)
    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Gson (Для конвертера списків у Room)
    implementation("com.google.code.gson:gson:2.10.1")

    // Activity & Fragment KTX (Для зручного отримання ViewModel)
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.7.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}