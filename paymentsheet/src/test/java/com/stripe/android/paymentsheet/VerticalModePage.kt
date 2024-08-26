package com.stripe.android.paymentsheet

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.stripe.android.model.PaymentMethodCode
import com.stripe.android.paymentsheet.ui.PAYMENT_SHEET_PRIMARY_BUTTON_TEST_TAG
import com.stripe.android.paymentsheet.verticalmode.TEST_TAG_NEW_PAYMENT_METHOD_ROW_BUTTON
import com.stripe.android.paymentsheet.verticalmode.TEST_TAG_PAYMENT_METHOD_VERTICAL_LAYOUT
import com.stripe.android.paymentsheet.verticalmode.TEST_TAG_SAVED_PAYMENT_METHOD_ROW_BUTTON
import com.stripe.android.paymentsheet.verticalmode.TEST_TAG_SAVED_TEXT
import com.stripe.android.paymentsheet.verticalmode.TEST_TAG_VIEW_MORE

internal class VerticalModePage(
    private val composeTestRule: ComposeTestRule
) {
    fun assertIsNotVisible() {
        composeTestRule
            .onNodeWithTag(TEST_TAG_PAYMENT_METHOD_VERTICAL_LAYOUT)
            .assertDoesNotExist()
    }

    fun waitUntilVisible() {
        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodes(hasTestTag(TEST_TAG_PAYMENT_METHOD_VERTICAL_LAYOUT))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    fun clickOnNewLpm(paymentMethodCode: PaymentMethodCode) {
        composeTestRule.onNode(hasTestTag("${TEST_TAG_NEW_PAYMENT_METHOD_ROW_BUTTON}_$paymentMethodCode"))
            .performScrollTo()
            .performClick()
    }

    fun assertPrimaryButton(matcher: SemanticsMatcher) {
        composeTestRule
            .onNode(hasTestTag(PAYMENT_SHEET_PRIMARY_BUTTON_TEST_TAG).and(matcher))
            .assertExists()
    }

    fun assertHasSavedPaymentMethods() {
        composeTestRule.onNodeWithTag(TEST_TAG_SAVED_TEXT).assertExists()
    }

    fun assertDoesNotHaveSavedPaymentMethods() {
        composeTestRule.onNodeWithTag(TEST_TAG_SAVED_TEXT).assertDoesNotExist()
    }

    fun assertHasSelectedSavedPaymentMethod(paymentMethodId: String) {
        composeTestRule.onNode(
            hasTestTag("${TEST_TAG_SAVED_PAYMENT_METHOD_ROW_BUTTON}_$paymentMethodId")
                .and(hasAnyDescendant(isSelected()))
        ).assertExists()
    }

    fun clickViewMore() {
        composeTestRule.onNodeWithTag(TEST_TAG_VIEW_MORE).performClick()
    }
}