package com.example.spinnshot.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/** Loads the canonical questions bundled inside the APK assets folder. */
class QuestionRepository(private val context: Context) {

    @Volatile
    private var cache: List<Question>? = null

    fun loadAll(): List<Question> {
        cache?.let { return it }
        val result = mutableListOf<Question>()
        context.assets.open(ASSET).use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
                val header = reader.readLine() ?: return emptyList()
                val columns = header.split(",").map { it.trim().lowercase() }
                val ic = columns.indexOf("categoria")
                val ip = columns.indexOf("pregunta")
                val ir = columns.indexOf("respuesta")
                if (ic < 0 || ip < 0 || ir < 0) return emptyList()

                var line = reader.readLine()
                while (line != null) {
                    val parts = parseCsvLine(line)
                    if (parts.size >= 3) {
                        result.add(
                            Question(
                                categoria = parts[ic].trim(),
                                pregunta = parts[ip].trim(),
                                respuesta = parts[ir].trim()
                            )
                        )
                    }
                    line = reader.readLine()
                }
            }
        }
        cache = result
        return result
    }

    fun byCategories(categories: Set<String>): List<Question> {
        val all = loadAll()
        if (categories.isEmpty() || categories.contains(Categories.ALL)) return all
        return all.filter { it.categoria in categories }
    }

    private fun parseCsvLine(line: String): List<String> {
        // Lightweight CSV parser. Supports quoted fields (with embedded commas).
        val out = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"'); i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    out.add(current.toString()); current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        out.add(current.toString())
        return out
    }

    companion object {
        private const val ASSET = "questions.csv"
    }
}
