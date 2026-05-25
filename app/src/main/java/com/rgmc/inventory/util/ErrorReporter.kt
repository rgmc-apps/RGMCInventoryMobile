package com.rgmc.inventory.util

import android.content.Intent
import android.net.Uri

data class ErrorReport(
    val endpoint: String,
    val statusCode: Int,
    val errorBody: String,
    val requestBody: String,
    val timestamp: String,
    val context: String = ""
)

object ErrorReporter {
    const val ERROR_EMAIL = "it.arellanoerwin@gmail.com"

    fun buildEmailIntent(report: ErrorReport): Intent {
        val subject = "[RGMC Inventory] API Error ${report.statusCode} — ${report.timestamp}"
        val body = buildString {
            appendLine("=== RGMC Inventory Error Report ===")
            appendLine()
            appendLine("Timestamp   : ${report.timestamp}")
            appendLine("Context     : ${report.context}")
            appendLine("Endpoint    : ${report.endpoint}")
            appendLine("Status Code : ${report.statusCode}")
            appendLine()
            appendLine("--- Request ---")
            appendLine(report.requestBody.ifEmpty { "(empty)" })
            appendLine()
            appendLine("--- Server Response ---")
            appendLine(report.errorBody.ifEmpty { "(no body)" })
        }
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ERROR_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
    }
}
