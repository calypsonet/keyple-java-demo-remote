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
package org.eclipse.keyple.demo.remote.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.eclipse.keyple.demo.remote.di.scopes.ActivityScoped
import org.eclipse.keyple.demo.remote.ui.CardReaderActivity
import org.eclipse.keyple.demo.remote.ui.CardSummaryActivity
import org.eclipse.keyple.demo.remote.ui.ChargeActivity
import org.eclipse.keyple.demo.remote.ui.ChargeResultActivity
import org.eclipse.keyple.demo.remote.ui.CheckoutActivity
import org.eclipse.keyple.demo.remote.ui.ConfigurationSettingsActivity
import org.eclipse.keyple.demo.remote.ui.HomeActivity
import org.eclipse.keyple.demo.remote.ui.PaymentValidatedActivity
import org.eclipse.keyple.demo.remote.ui.PersonnalizationActivity
import org.eclipse.keyple.demo.remote.ui.SelectTicketsActivity
import org.eclipse.keyple.demo.remote.ui.ServerSettingsActivity
import org.eclipse.keyple.demo.remote.ui.SettingsMenuActivity
import org.eclipse.keyple.demo.remote.ui.SplashScreenActivity

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
