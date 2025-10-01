package com.example.loginpage.data

import android.content.Context
import android.util.Log
import com.example.loginpage.model.Perfume

object CartManager {
    private const val TAG = "CartManager"
    private val cart = mutableListOf<Perfume>()

    // Separate in-memory cart for items coming from Local/Rapid API (string-based)
    private val remoteCart = mutableListOf<LocalPerfume>()

    private var appContext: Context? = null
    private var idToPerfume: Map<Int, Perfume> = emptyMap()

    fun init(context: Context) {
        appContext = context.applicationContext
        // Build lookup map (men + women) once
        val ds = DataSource()
        idToPerfume = (ds.loadMenPerfumes() + ds.loadWomenPerfumes()).associateBy { it.id }
        restore()
    }

    private fun restore() {
        val ctx = appContext ?: return
        val storedIds = LocalStorage.loadCartIds(ctx)
        cart.clear()
        storedIds.forEach { id ->
            idToPerfume[id]?.let { cart.add(it) }
        }
        Log.d(TAG, "Restored cart with ${cart.size} items from storage")
    }

    private fun persist() {
        val ctx = appContext ?: return
        LocalStorage.saveCartIds(ctx, cart.map { it.id })
    }

    // ---------- Resource-based items ----------
    fun addToCart(perfume: Perfume) {
        cart.add(perfume)
        persist()
    }

    fun getCartItems(): List<Perfume> = cart

    fun removeFromCart(perfumeId: Int) {
        val removed = cart.removeIf { it.id == perfumeId }
        if (removed) persist()
    }

    // ---------- Remote/local string-based items ----------
    fun addToCart(local: LocalPerfume) {
        remoteCart.add(local)
        // Not persisted yet (no schema); still fulfills user workflow
    }

    fun getRemoteCartItems(): List<LocalPerfume> = remoteCart

    fun removeRemoteFromCart(perfumeId: Int) {
        remoteCart.removeAll { it.id == perfumeId }
    }

    fun clearCart() {
        cart.clear()
        remoteCart.clear()
        persist()
    }
}
