package com.example.CLYRedNote.model

import java.math.BigDecimal
import java.util.Date

data class CartItem(
    val id: String,
    val product: Product,
    val quantity: Int,
    val selectedSpecs: Map<String, String> = emptyMap(),
    val isSelected: Boolean = true,
    val addedAt: Date = Date()
)

data class ShoppingCart(
    val userId: String,
    val items: List<CartItem> = emptyList(),
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val totalOriginalAmount: BigDecimal = BigDecimal.ZERO,
    val totalDiscount: BigDecimal = BigDecimal.ZERO,
    val updatedAt: Date = Date()
)

data class Order(
    val id: String,
    val orderNumber: String,
    val userId: String,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val shippingFee: BigDecimal = BigDecimal.ZERO,
    val discountAmount: BigDecimal = BigDecimal.ZERO,
    val actualAmount: BigDecimal,
    val shippingAddress: Address,
    val note: String? = null,
    val paymentMethod: PaymentMethod? = null,
    val paymentTime: Date? = null,
    val shippingTime: Date? = null,
    val deliveryTime: Date? = null,
    val completionTime: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val trackingNumber: String? = null
)

data class OrderItem(
    val id: String,
    val product: Product,
    val quantity: Int,
    val price: BigDecimal,
    val selectedSpecs: Map<String, String> = emptyMap(),
    val status: OrderItemStatus = OrderItemStatus.NORMAL
)

enum class OrderStatus {
    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    REFUND_REQUESTED,
    REFUNDED
}

enum class OrderItemStatus {
    NORMAL,
    AFTER_SALES_REQUESTED,
    AFTER_SALES_PROCESSING,
    AFTER_SALES_COMPLETED
}

data class Address(
    val id: String,
    val recipientName: String,
    val phoneNumber: String,
    val province: String,
    val city: String,
    val district: String,
    val detailAddress: String,
    val postalCode: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Date = Date()
)

data class PaymentMethod(
    val type: PaymentType,
    val name: String,
    val description: String? = null
)

enum class PaymentType {
    WECHAT_PAY,
    ALIPAY,
    BANK_CARD,
    CREDIT_CARD,
    BALANCE
}

data class AfterSalesRequest(
    val id: String,
    val orderId: String,
    val orderItemId: String,
    val type: AfterSalesType,
    val reason: String,
    val description: String,
    val images: List<String> = emptyList(),
    val status: AfterSalesStatus,
    val createdAt: Date = Date(),
    val processedAt: Date? = null,
    val completedAt: Date? = null
)

enum class AfterSalesType {
    REFUND_ONLY,
    RETURN_REFUND,
    EXCHANGE
}

enum class AfterSalesStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PROCESSING,
    COMPLETED
}