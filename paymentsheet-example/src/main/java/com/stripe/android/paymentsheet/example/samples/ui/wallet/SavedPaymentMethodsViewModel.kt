package com.stripe.android.paymentsheet.example.samples.ui.wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.requests.suspendable
import com.stripe.android.ExperimentalSavedPaymentMethodsApi
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.example.samples.networking.ExampleSavedPaymentMethodRequest
import com.stripe.android.paymentsheet.example.samples.networking.ExampleSavedPaymentMethodResponse
import com.stripe.android.paymentsheet.example.samples.networking.awaitModel
import com.stripe.android.paymentsheet.repositories.CustomerAdapter
import com.stripe.android.paymentsheet.repositories.CustomerEphemeralKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.github.kittinunf.result.Result as FuelResult

@OptIn(ExperimentalSavedPaymentMethodsApi::class)
class SavedPaymentMethodsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow<SavedPaymentMethodsViewState>(
        value = SavedPaymentMethodsViewState.Loading
    )
    val state: StateFlow<SavedPaymentMethodsViewState> = _state

    internal val customerAdapter: CustomerAdapter = CustomerAdapter.create(
        context = getApplication(),
        customerEphemeralKeyProvider = {
            fetchCustomerEphemeralKey().fold(
                success = {
                    Result.success(
                        CustomerEphemeralKey.create(
                            customerId = it.customerId,
                            ephemeralKey = it.customerEphemeralKeySecret
                        )
                    )
                },
                failure = {
                    Result.failure(it.exception)
                }
            )
        },
        setupIntentClientSecretProvider = null,
    )

    init {
        viewModelScope.launch {
            initialize()
        }
    }

    private suspend fun initialize() {
        when (val result = fetchCustomerEphemeralKey()) {
            is FuelResult.Success -> {
                PaymentConfiguration.init(
                    context = getApplication(),
                    publishableKey = result.value.publishableKey,
                )
                _state.update {
                    SavedPaymentMethodsViewState.Data(
                        customerEphemeralKey = CustomerEphemeralKey.create(
                            customerId = result.value.customerId,
                            ephemeralKey = result.value.customerEphemeralKeySecret,
                        )
                    )
                }
            }
            is FuelResult.Failure -> {
                _state.update {
                    SavedPaymentMethodsViewState.FailedToLoad(
                        message = result.error.message.toString()
                    )
                }
            }
        }
    }

    private suspend fun fetchCustomerEphemeralKey():
        FuelResult<ExampleSavedPaymentMethodResponse, FuelError> {
        val request = ExampleSavedPaymentMethodRequest(
            customerType = "returning"
        )
        val requestBody = Json.encodeToString(
            ExampleSavedPaymentMethodRequest.serializer(),
            request
        )

        return Fuel
            .post("$backendUrl/customer_ephemeral_key")
            .jsonBody(requestBody)
            .suspendable()
            .awaitModel(ExampleSavedPaymentMethodResponse.serializer())
    }

    private companion object {
        const val backendUrl = "https://glistening-heavenly-radon.glitch.me"
    }
}
