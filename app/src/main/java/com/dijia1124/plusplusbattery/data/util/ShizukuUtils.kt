package com.dijia1124.plusplusbattery.data.util

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuUtils {

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasShizukuPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int = 1) {
        if (!isShizukuAvailable()) return
        if (hasShizukuPermission()) return
        try {
            Shizuku.requestPermission(requestCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun executeCommand(command: String): String? = withContext(Dispatchers.IO) {
        if (!hasShizukuPermission()) return@withContext null
        try {
            // Using reflection to bypass Kotlin's "private static" error since Shizuku.newProcess is public in Java
            val method = Shizuku::class.java.getMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            if (process.exitValue() == 0) {
                output.toString().trim()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
