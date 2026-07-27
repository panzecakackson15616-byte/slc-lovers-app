package com.slclovers.app.sync

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * GitHub Contents API 客户端
 */
class GitHubClient(
    private val token: String,
    private val owner: String,
    private val repo: String
) {
    private val baseUrl = "https://api.github.com"

    /**
     * 检查仓库是否存在
     */
    suspend fun checkRepoExists(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/repos/$owner/$repo")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            addHeaders(conn)
            conn.responseCode == 200
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 读取文件
     * @return Pair<原始字节, sha>，文件不存在返回 null
     */
    suspend fun readFile(path: String): Pair<ByteArray, String>? = withContext(Dispatchers.IO) {
        val encodedPath = path.split("/").joinToString("/") { java.net.URLEncoder.encode(it, "UTF-8") }
        val url = URL("$baseUrl/repos/$owner/$repo/contents/$encodedPath")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        addHeaders(conn)

        try {
            if (conn.responseCode == 404) return@withContext null
            if (conn.responseCode != 200) {
                throw GitHubException("HTTP ${conn.responseCode}: ${conn.errorStream?.bufferedReader()?.readText() ?: ""}")
            }

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val content = json.getString("content").replace("\n", "")
            val sha = json.getString("sha")
            val data = Base64.decode(content, Base64.DEFAULT)
            data to sha
        } finally {
            conn.disconnect()
        }
    }

    /**
     * 写入/更新文件
     * @return 新的 commit sha
     */
    suspend fun writeFile(
        path: String,
        data: ByteArray,
        sha: String? = null,
        message: String
    ): String = withContext(Dispatchers.IO) {
        val encodedPath = path.split("/").joinToString("/") { java.net.URLEncoder.encode(it, "UTF-8") }
        val url = URL("$baseUrl/repos/$owner/$repo/contents/$encodedPath")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        addHeaders(conn)
        conn.doOutput = true

        val payload = JSONObject().apply {
            put("message", message)
            put("content", Base64.encodeToString(data, Base64.NO_WRAP))
            if (sha != null) put("sha", sha)
        }

        conn.outputStream.use { it.write(payload.toString().toByteArray()) }

        val body = if (conn.responseCode in 200..299) {
            conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            throw GitHubException("HTTP ${conn.responseCode}: ${conn.errorStream?.bufferedReader()?.readText() ?: ""}")
        }

        JSONObject(body).optJSONObject("commit")?.getString("sha") ?: ""
    }

    /**
     * 删除文件
     */
    suspend fun deleteFile(path: String, sha: String, message: String) = withContext(Dispatchers.IO) {
        val encodedPath = path.split("/").joinToString("/") { java.net.URLEncoder.encode(it, "UTF-8") }
        val url = URL("$baseUrl/repos/$owner/$repo/contents/$encodedPath")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "DELETE"
        addHeaders(conn)
        conn.doOutput = true

        val payload = JSONObject().apply {
            put("message", message)
            put("sha", sha)
        }
        conn.outputStream.use { it.write(payload.toString().toByteArray()) }

        if (conn.responseCode !in 200..299) {
            throw GitHubException("HTTP ${conn.responseCode}")
        }
    }

    private fun addHeaders(conn: HttpURLConnection) {
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.setRequestProperty("User-Agent", "SLC-Lovers/1.0")
        conn.setRequestProperty("Content-Type", "application/json")
    }
}

class GitHubException(message: String) : Exception(message)