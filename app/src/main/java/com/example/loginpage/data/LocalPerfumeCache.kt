package com.example.loginpage.data

import java.util.concurrent.ConcurrentHashMap

object LocalPerfumeCache {
    private val map = ConcurrentHashMap<Int, LocalPerfume>()

    @Synchronized
    fun update(list: List<LocalPerfume>) {
        list.forEach { map[it.id] = it }
    }

    @Synchronized
    fun get(id: Int): LocalPerfume? = map[id]

    @Synchronized
    fun clear() { map.clear() }

    // New: snapshot of all cached perfumes for search/autocomplete
    @Synchronized
    fun all(): List<LocalPerfume> = map.values.toList()
}
