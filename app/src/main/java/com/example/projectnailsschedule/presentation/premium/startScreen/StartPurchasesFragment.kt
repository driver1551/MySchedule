package com.example.projectnailsschedule.presentation.premium.startScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.FragmentStartPurchasesBinding
import com.example.projectnailsschedule.domain.models.rustoreBilling.StartPurchasesEvent
import com.example.projectnailsschedule.domain.models.rustoreBilling.StartPurchasesState
import com.example.projectnailsschedule.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.rustore.sdk.billingclient.model.purchase.PurchaseAvailabilityResult
import ru.rustore.sdk.billingclient.utils.resolveForBilling

/** Фрагмент, который проверяет, что платежи доступны и пользователи могут совершать покупки.
 * + Добавлена проверка, что пользователь залогинен в аккаунте и русторе */

@AndroidEntryPoint
class StartPurchasesFragment : Fragment() {

    private val viewModel: StartPurchasesViewModel by viewModels()

    private var binding: FragmentStartPurchasesBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartPurchasesBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.initView()

        // подписка на state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect { state ->
                    binding?.updateState(state)
                }
        }

        // подписка на event
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect { event ->
                    handleEvent(event)
                }
        }
    }

    private fun FragmentStartPurchasesBinding.initView() {
        // Проверяет доступность покупок, если все ок - отправляет event на фрагмент
        startPurchasesButton.setOnClickListener {
            viewModel.checkPurchasesAvailability()
        }

        swipeRefreshLayout.setOnRefreshListener {
            // Проверяем залогинился ли пользователь в свой акк и в RuStore
            viewModel.checkLogin()
        }
    }

    private fun FragmentStartPurchasesBinding.updateState(state: StartPurchasesState) {
        with(state.isLoading) {
            startPurchasesButton.isEnabled = !this
            swipeRefreshLayout.isRefreshing = this
        }

//        rustoreLoginStatus.setImageResource(
//            if (state.isRuStoreLoggedIn == true) R.drawable.baseline_check_circle_outline_24
//            else R.drawable.outline_cancel_24
//        )

        serverLoginStatus.setImageResource(
            if (state.isAccountLoggedIn == true) R.drawable.baseline_check_circle_outline_24
            else R.drawable.outline_cancel_24
        )

        binding?.startPurchasesButton?.isEnabled =
            state.isRuStoreLoggedIn == true && state.isAccountLoggedIn == true
    }

    private fun handleEvent(event: StartPurchasesEvent) {
        when (event) {
            is StartPurchasesEvent.PurchasesAvailability -> {
                when (event.availability) {
                    is PurchaseAvailabilityResult.Available -> {
                        this@StartPurchasesFragment.findNavController().navigate(
                            R.id.action_nav_premium_start_to_nav_premium
                        )
                    }

                    is PurchaseAvailabilityResult.Unknown -> {
                        this@StartPurchasesFragment.findNavController().navigate(
                            R.id.action_nav_premium_start_to_nav_premium
                        )
                    }

                    is PurchaseAvailabilityResult.Unavailable -> {
                        event.availability.cause.resolveForBilling(requireContext())
                        event.availability.cause.message?.let(::showToast)
                    }

                    else -> {}
                }
            }

            is StartPurchasesEvent.Error -> {
                showToast(event.throwable.message ?: getString(R.string.billing_common_error))
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
