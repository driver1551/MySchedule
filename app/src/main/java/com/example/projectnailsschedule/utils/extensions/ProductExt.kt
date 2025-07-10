package com.example.projectnailsschedule.utils.extensions

import androidx.annotation.StringRes
import com.example.projectnailsschedule.R
import ru.rustore.sdk.billingclient.model.product.ProductStatus
import ru.rustore.sdk.billingclient.model.product.ProductType

@StringRes
fun ProductType.getStringRes(): Int {
    return when (this) {
        ProductType.NON_CONSUMABLE -> R.string.billing_product_type_non_consumable
        ProductType.CONSUMABLE -> R.string.billing_product_type_consumable
        ProductType.SUBSCRIPTION -> R.string.billing_product_type_subscription
    }
}

@StringRes
fun ProductStatus.getStringRes(): Int {
    return when (this) {
        ProductStatus.ACTIVE -> R.string.billing_product_status_active
        ProductStatus.INACTIVE -> R.string.billing_product_status_inactive
    }
}
