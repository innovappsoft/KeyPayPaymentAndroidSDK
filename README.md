# KeyPay Payment Android SDK

Official Android SDK for accepting KeyPay PaymentIntent payments from Kotlin or Java apps.

The SDK gives developers a native checkout flow similar to the iOS `KeyPayPaymentSDK`:

1. The merchant app asks its own backend to create a PaymentIntent.
2. The backend creates the intent with the private `dk_live` key.
3. The SDK presents a native KeyPay payment sheet.
4. The SDK opens the returned `checkout_url` in KeyCard/KeyPay checkout.
5. The SDK checks the PaymentIntent status and returns a result.

> Never put a `dk_live` key inside an Android app. The app talks to your backend; your backend talks to KeyPay.

## Features

- Kotlin-first SDK with Java-compatible APIs.
- Drop-in `KeyPayCheckoutButton`.
- Native Android payment sheet.
- English and Spanish localizations.
- Small HTTP layer with no required third-party networking dependency.
- Secure backend PaymentIntent flow.

## Install

The SDK source lives on GitHub, but Android projects do not automatically consume GitHub source code as a Gradle dependency. Developers must use one of the installation modes below.

### Option A — JitPack dependency

Use this when you want Gradle to download the SDK from GitHub like a normal dependency.

In your project `settings.gradle.kts`, add JitPack:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

In `gradle/libs.versions.toml`:

```toml
[versions]
keypaySdkVersion = "v1.0.1"

[libraries]
keypay-android-sdk = { module = "com.github.innovappsoft.KeyPayPaymentAndroidSDK:keypay-payment-sdk", version.ref = "keypaySdkVersion" }
```

In your app module `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.keypay.android.sdk)
}
```

Then run **Sync Now**, **Clean Project** and **Rebuild Project**.

> Important: use `v1.0.1` or newer. `v1.0.0` was a source-only GitHub release and did not include the Maven publishing metadata needed by JitPack.

### Option B — Local AAR for testing

Use this when testing locally before publishing or when JitPack is still building the release.

Build the SDK AAR from this repository:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :keypay-payment-sdk:assembleRelease
```

Copy the generated file into your app:

```text
keypay-payment-sdk/build/outputs/aar/keypay-payment-sdk-release.aar
```

to:

```text
your-app/app/libs/keypay-payment-sdk-release.aar
```

In your app module `build.gradle.kts`:

```kotlin
dependencies {
    implementation(files("libs/keypay-payment-sdk-release.aar"))
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
}
```

The `appcompat` and `material` lines are required because local AAR files do not automatically bring all transitive dependencies.

### Option C — Local module development

Use this only when you are actively editing the SDK and the app at the same time.

In your app project `settings.gradle.kts`:

```kotlin
include(":keypay-payment-sdk")
project(":keypay-payment-sdk").projectDir = file("../KeyPayPaymentAndroidSDK/keypay-payment-sdk")
```

In your app module `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":keypay-payment-sdk"))
}
```

If Gradle reports a plugin classpath conflict when using this mode, switch to Option A or Option B. Local module development can conflict when the host app and SDK project define plugin versions differently.

### Common install errors

- `Could not find com.github.innovappsoft.KeyPayPaymentAndroidSDK:keypay-payment-sdk:v1.0.0`: use `v1.0.1` or newer, or use the local AAR option.
- `KeyPayReturnActivity` appears red in Android Studio: the SDK dependency is not resolved yet. Sync Gradle and confirm the dependency is present.
- `Cannot generate dependency accessors`: your `libs.versions.toml` has duplicate aliases. Use `keypaySdkVersion` for the version and `keypay-android-sdk` for the library.
- `androidx.core:core-ktx:1.19.0 requires compileSdk 37 / AGP 9.1`: lower `coreKtx` to `1.17.0` when using AGP `8.13.x` and `compileSdk 36`, or update your Android Gradle plugin and compileSdk.

## Configure

Call `KeyPay.configure(...)` once when your app starts.

```kotlin
import com.innovappsoft.keypay.payment.KeyPay
import com.innovappsoft.keypay.payment.KeyPayConfiguration

KeyPay.configure(
    KeyPayConfiguration(
        paymentIntentEndpoint = "https://your-backend.com/api/keypay/payment-intents",
        paymentStatusEndpoint = "https://your-backend.com/api/keypay/payment-intents",
        returnUrlScheme = "myapp",
        merchantDisplayName = "My App"
    )
)
```

## Kotlin: checkout button

```kotlin
import com.innovappsoft.keypay.payment.KeyPayCheckoutButton
import com.innovappsoft.keypay.payment.KeyPayPaymentRequest
import com.innovappsoft.keypay.payment.KeyPayPaymentResult

val request = KeyPayPaymentRequest(
    amount = "10.00",
    title = "Premium Plan",
    description = "Monthly subscription",
    externalReference = "order_1001",
    metadata = mapOf("order_id" to "1001")
)

val button = KeyPayCheckoutButton(this).apply {
    configure(
        activity = this@MainActivity,
        request = request,
        callback = { result ->
            when (result.status) {
                KeyPayPaymentResult.Status.SUCCEEDED -> {}
                KeyPayPaymentResult.Status.PENDING -> {}
                KeyPayPaymentResult.Status.CANCELLED -> {}
                KeyPayPaymentResult.Status.FAILED -> {}
            }
        },
        title = "Pay with KeyPay"
    )
}
```

## Kotlin: direct checkout

```kotlin
KeyPay.startCheckout(this, request) { result ->
    when (result.status) {
        KeyPayPaymentResult.Status.SUCCEEDED -> {
            val intentId = result.intent?.id
        }
        KeyPayPaymentResult.Status.PENDING -> {}
        KeyPayPaymentResult.Status.CANCELLED -> {}
        KeyPayPaymentResult.Status.FAILED -> {
            val error = result.error
        }
    }
}
```

## Java

```java
import com.innovappsoft.keypay.payment.KeyPay;
import com.innovappsoft.keypay.payment.KeyPayConfiguration;
import com.innovappsoft.keypay.payment.KeyPayPaymentRequest;
import com.innovappsoft.keypay.payment.KeyPayPaymentResult;

KeyPay.configure(new KeyPayConfiguration(
    "https://your-backend.com/api/keypay/payment-intents",
    "https://your-backend.com/api/keypay/payment-intents",
    "myapp",
    "My App"
));

KeyPayPaymentRequest request = new KeyPayPaymentRequest(
    "10.00",
    "Premium Plan"
);

KeyPay.startCheckout(this, request, result -> {
    if (result.getStatus() == KeyPayPaymentResult.Status.SUCCEEDED) {
        String intentId = result.getIntent().getId();
    }
});
```

## AndroidManifest callback

Your app must register the return URL scheme that you passed in `returnUrlScheme`.

```xml
<activity
    android:name="com.innovappsoft.keypay.payment.KeyPayReturnActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="myapp" android:host="keypay-return" />
    </intent-filter>
</activity>
```

## Backend contract

The Android app should not create PaymentIntents directly with the developer API key. Your backend should expose:

- `POST /api/keypay/payment-intents`
- `GET /api/keypay/payment-intents/{id}`

The SDK sends a JSON payload like:

```json
{
  "amount": "10.00",
  "currency": "KCOIN",
  "title": "Premium Plan",
  "description": "Monthly subscription",
  "external_reference": "order_1001",
  "return_url": "myapp://keypay-return",
  "platform": "android",
  "metadata": {
    "order_id": "1001"
  }
}
```

Your backend returns the KeyPay PaymentIntent envelope:

```json
{
  "success": true,
  "code": "ok",
  "message": "PaymentIntent created successfully.",
  "data": {
    "id": "pi_xxx",
    "status": "requires_payment",
    "amount": "10.00",
    "currency": "KCOIN",
    "checkout_url": "https://keypay.innovapp-soft.com/pay/...",
    "client_secret": "kpi_xxx"
  }
}
```

## Modules

- `keypay-payment-sdk`: reusable Android SDK module.
- `sample-app`: local sample app for testing integration.

## Build

This project requires Java 11 or newer. If your terminal uses Java 8, run with Android Studio's JBR:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :keypay-payment-sdk:assembleDebug :sample-app:assembleDebug
```
