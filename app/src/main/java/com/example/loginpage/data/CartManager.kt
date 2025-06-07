package com.example.loginpage.data

import com.example.loginpage.model.Perfume

object CartManager {
    private val cart = mutableListOf<Perfume>()

    fun addToCart(perfume: Perfume) {
        cart.add(perfume)
    }

    fun getCartItems(): List<Perfume> {
        return cart
    }

    fun removeFromCart(perfumeId: Int) {
        cart.removeIf { it.id == perfumeId }
    }

    fun clearCart() {
        cart.clear()
    }
}
