package com.example.CLYRedNote.model

import java.math.BigDecimal
import java.util.Date

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val brand: String? = null,
    val category: String,
    val price: BigDecimal,
    val originalPrice: BigDecimal? = null,
    val discountRate: Double? = null,
    val images: List<String> = emptyList(),
    val thumbnailImage: String? = null,
    val sellerId: String,
    val sellerName: String,
    val stock: Int = 0,
    val salesCount: Int = 0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val tags: List<String> = emptyList(),
    val specifications: Map<String, String> = emptyMap(),
    val promotions: List<Promotion> = emptyList(),
    val shippingInfo: ShippingInfo? = null,
    val status: ProductStatus = ProductStatus.AVAILABLE,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class ProductStatus {
    AVAILABLE,
    OUT_OF_STOCK,
    DISCONTINUED,
    DELETED
}

data class Promotion(
    val id: String,
    val type: PromotionType,
    val name: String,
    val description: String,
    val discountAmount: BigDecimal? = null,
    val discountRate: Double? = null,
    val minOrderAmount: BigDecimal? = null,
    val startTime: Date,
    val endTime: Date,
    val isActive: Boolean = true
)

enum class PromotionType {
    DISCOUNT,
    FULL_REDUCTION,
    BUY_ONE_GET_ONE,
    SHIPPING_FREE,
    COMBO_DEAL
}

data class ShippingInfo(
    val freeShippingThreshold: BigDecimal? = null,
    val shippingFee: BigDecimal = BigDecimal.ZERO,
    val estimatedDays: Int = 3,
    val regions: List<String> = emptyList()
)

data class ProductReview(
    val id: String,
    val productId: String,
    val userId: String,
    val username: String,
    val userAvatar: String? = null,
    val rating: Int,
    val content: String,
    val images: List<String> = emptyList(),
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Date = Date(),
    val specs: Map<String, String> = emptyMap()
)