package com.tuta.auto.util

import kotlin.random.Random

object NameGenerator {
    private const val PREFIXES = "abcdefghijklmnopqrstuvwxyz"
    private const val CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"

    fun generateEmailPrefix(length: Int = 12): String {
        val sb = StringBuilder()
        sb.append(PREFIXES[Random.nextInt(PREFIXES.length)])
        repeat(length - 1) {
            sb.append(CHARS[Random.nextInt(CHARS.length)])
        }
        return sb.toString()
    }

    fun generatePassword(length: Int = 16): String {
        val all = CHARS + "ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#_-."
        return (1..length)
            .map { all[Random.nextInt(all.length)] }
            .joinToString("")
    }
}
