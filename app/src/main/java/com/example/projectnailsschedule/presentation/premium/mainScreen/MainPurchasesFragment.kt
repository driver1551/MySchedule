package com.example.projectnailsschedule.presentation.premium.mainScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.FragmentPremiumBinding
import com.example.projectnailsschedule.domain.models.rustoreBilling.BillingEvent
import com.example.projectnailsschedule.domain.models.rustoreBilling.BillingState
import com.example.projectnailsschedule.utils.extensions.showAlertDialog
import com.example.projectnailsschedule.utils.extensions.showToast
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.rustore.sdk.billingclient.utils.resolveForBilling
import ru.rustore.sdk.core.exception.RuStoreException

@AndroidEntryPoint
class MainPurchasesFragment : Fragment() {
    private var binding: FragmentPremiumBinding? = null

    private val viewModel: MainPurchasesFragmentViewModel by viewModels()

    private val productsAdapter = ProductsAdapter(onProductClick = { product -> viewModel.onProductClick(product) })
    private val purchasesAdapter = PurchasesAdapter()

    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPremiumBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.initViews()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect { state ->
                    binding?.updateState(state)
                }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect { event ->
                    handleEvent(event)
                }
        }
    }

    override fun onDestroyView() {
        snackbar = null
        binding = null
        super.onDestroyView()
    }

    private fun FragmentPremiumBinding.initViews() {
        productsRecycler.adapter = productsAdapter
        purchasesRecycler.adapter = purchasesAdapter
        swipeRefreshLayout.setOnRefreshListener { viewModel.updateProductsAndPurchases() }
    }

    private fun FragmentPremiumBinding.updateState(state: BillingState) {
        swipeRefreshLayout.isRefreshing = state.isLoading

        emptyProductsView.isVisible = state.isEmpty

        productsAdapter.submitList(state.products)

        purchasesAdapter.submitList(state.purchases)

        if (state.snackbarResId != null) {
            snackbar = Snackbar.make(root, state.snackbarResId, Snackbar.LENGTH_INDEFINITE)
            snackbar?.show()
        } else {
            snackbar?.dismiss()
        }
    }

    private fun handleEvent(event: BillingEvent) {
        when (event) {
            is BillingEvent.ShowDialog -> {
                requireContext().showAlertDialog(
                    title = getString(event.dialogInfo.titleRes),
                    message = event.dialogInfo.message,
                    positiveText = "Перейти",
                    onPositiveClick = {
                        findNavController().navigate(R.id.action_nav_purchases_main_fragment_to_nav_settings)
                    },
                    negativeText = "Позже"
                )
            }

            is BillingEvent.ShowError -> {
                if (event.error is RuStoreException) {
                    event.error.resolveForBilling(requireContext())
                }
                showToast(
                    message = "${getString(R.string.billing_general_error)}: ${event.error.message.orEmpty()}",
                    lengthLong = true
                )
            }

            is BillingEvent.RefreshPurchases -> {
                viewModel.updateProductsAndPurchases()
            }
        }
    }
}
