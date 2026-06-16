package com.innovappsoft.keypay.payment

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Internal HTTP client used by the SDK.
 *
 * It intentionally avoids third-party networking dependencies so the SDK stays
 * small and easy to consume from any Android project.
 */
internal class KeyPayHttpClient(private val timeoutMillis: Int) {
    fun postJson(url: String, body: String): String {
        val connection = openConnection(url, "POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(body)
        }

        return readResponse(connection)
    }

    fun get(url: String): String {
        val connection = openConnection(url, "GET")
        return readResponse(connection)
    }

    private fun openConnection(rawUrl: String, method: String): HttpURLConnection {
        val connection = URL(rawUrl).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = timeoutMillis
        connection.readTimeout = timeoutMillis
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "KeyPayPaymentAndroidSDK/${KeyPaySDKInfo.version}")
        return connection
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
        }.orEmpty()

        if (code !in 200..299) {
            throw KeyPayCheckoutException("http_error", "HTTP $code: $body")
        }

        return body
    }
}
