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

For local development, add the module to your app:

```kotlin
dependencies {
    implementation(project(":keypay-payment-sdk"))
}
```

When published to Maven/JitPack later, developers will use a normal dependency like:

```kotlin
dependencies {
    implementation("com.innovappsoft:keypay-payment-android-sdk:1.0.0")
}
```

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
