import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.innovappsoft.keypay.payment"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.innovappsoft.KeyPayPaymentAndroidSDK"
            artifactId = "keypay-payment-sdk"
            version = "1.0.1"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("KeyPay Payment Android SDK")
                description.set("Official Android SDK for accepting KeyPay PaymentIntent payments from Kotlin or Java apps.")
                url.set("https://github.com/innovappsoft/KeyPayPaymentAndroidSDK")
            }
        }
    }
}
