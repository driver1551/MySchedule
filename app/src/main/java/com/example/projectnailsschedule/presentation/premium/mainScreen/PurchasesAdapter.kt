package com.example.projectnailsschedule.presentation.premium.mainScreen

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.ItemProductViewHolderBinding
import com.example.projectnailsschedule.utils.createDiffUtil
import ru.rustore.sdk.billingclient.model.purchase.Purchase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar

class PurchasesAdapter : ListAdapter<Purchase, PurchasesAdapter.ViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_view_holder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemProductViewHolderBinding.bind(itemView)

        @SuppressLint("SetTextI18n")
        fun bind(purchase: Purchase) {
            val f = SimpleDateFormat.getDateInstance()
            val startSubscription = purchase.purchaseTime!!
            val endSubscription = Calendar.getInstance().apply {
                time = startSubscription
                add(Calendar.MONTH, 1)
            }
            with(binding) {
                titleView.text = "Подписка"
                productStatusView.text =
                    "Подписка активна с ${f.format(startSubscription)} по ${f.format((endSubscription as GregorianCalendar).time)}"
            }
        }
    }

    companion object {
        private val DIFF_UTIL = createDiffUtil<Purchase> { oldItem, newItem ->
            oldItem.productId == newItem.productId
        }
    }
}
