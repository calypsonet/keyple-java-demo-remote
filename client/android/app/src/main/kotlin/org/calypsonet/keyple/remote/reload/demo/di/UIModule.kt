/********************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.calypsonet.keyple.remote.reload.demo.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.calypsonet.keyple.remote.reload.demo.di.scopes.ActivityScoped
import org.calypsonet.keyple.remote.reload.demo.ui.CardReaderActivity
import org.calypsonet.keyple.remote.reload.demo.ui.CardSummaryActivity
import org.calypsonet.keyple.remote.reload.demo.ui.ChargeActivity
import org.calypsonet.keyple.remote.reload.demo.ui.ChargeResultActivity
import org.calypsonet.keyple.remote.reload.demo.ui.CheckoutActivity
import org.calypsonet.keyple.remote.reload.demo.ui.ConfigurationSettingsActivity
import org.calypsonet.keyple.remote.reload.demo.ui.HomeActivity
import org.calypsonet.keyple.remote.reload.demo.ui.PaymentValidatedActivity
import org.calypsonet.keyple.remote.reload.demo.ui.PersonnalizationActivity
import org.calypsonet.keyple.remote.reload.demo.ui.SelectTicketsActivity
import org.calypsonet.keyple.remote.reload.demo.ui.ServerSettingsActivity
import org.calypsonet.keyple.remote.reload.demo.ui.SettingsMenuActivity
import org.calypsonet.keyple.remote.reload.demo.ui.SplashScreenActivity

@Module
abstract class UIModule {
    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun splashScreenActivity(): SplashScreenActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun homeActivity(): HomeActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun configurationSettingsActivity(): ConfigurationSettingsActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun serverSettingsActivity(): ServerSettingsActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun settingsMenuActivity(): SettingsMenuActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun cardReaderActivity(): CardReaderActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun cardSummaryActivity(): CardSummaryActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun selectTicketsActivity(): SelectTicketsActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun checkoutActivity(): CheckoutActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun paymentValidatedActivity(): PaymentValidatedActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun chargeCardActivity(): ChargeActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun chargeResultActivity(): ChargeResultActivity?

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun personnalizationActivity(): PersonnalizationActivity?
}