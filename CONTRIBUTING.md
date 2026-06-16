# Contributing

Thanks for helping improve KeyPay Payment Android SDK.

## Development

- Keep the public API compatible with Kotlin and Java callers.
- Do not place private KeyPay developer keys inside examples or tests.
- Run the Android build before opening a pull request.

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :keypay-payment-sdk:assembleDebug :sample-app:assembleDebug
```
