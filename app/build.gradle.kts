plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")


}

android {
    namespace = "com.lumoo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lumoo"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "1.6"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/*.kotlin_module")
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-video:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")
    implementation ("androidx.camera:camera-extensions:1.3.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))

    implementation ("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-storage")
    implementation ("com.google.firebase:firebase-firestore")

    implementation ("com.google.gms:google-services:4.3.15")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-core:21.1.1")

    implementation ("com.firebaseui:firebase-ui-database:8.0.2")


    implementation ("com.google.android.libraries.places:places:2.5.0")

    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.mlkit:translate:17.0.1")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")





    implementation ("com.firebaseui:firebase-ui-database:8.0.2")


    implementation ("com.google.firebase:firebase-messaging:23.0.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")

    implementation ("com.github.shts:StoriesProgressView:3.0.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation (libs.express.video)

    implementation (libs.lottie)


    implementation (libs.billing)

    implementation ("com.google.android.material:material:1.9.0")


    implementation (libs.zego.uikit.prebuilt.call.android)  // add this line in your module-level build.gradle file's dependencies, usually named [app].
    implementation ("im.zego:express-video:3.22.0")
    implementation ("com.mikhaellopez:circularimageview:4.3.0")







}