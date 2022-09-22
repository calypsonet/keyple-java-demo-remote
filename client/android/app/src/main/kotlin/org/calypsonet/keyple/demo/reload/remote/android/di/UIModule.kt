/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.calypsonet.keyple.demo.reload.remote.android.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.calypsonet.keyple.demo.reload.remote.android.activity.CardReaderActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.CardSummaryActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.ChargeActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.ChargeResultActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.CheckoutActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.ConfigurationSettingsActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.HomeActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.PaymentValidatedActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.PersonnalizationActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.SelectTicketsActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.ServerSettingsActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.SettingsMenuActivity
import org.calypsonet.keyple.demo.reload.remote.android.activity.SplashScreenActivity
import org.calypsonet.keyple.demo.reload.remote.android.di.scopes.ActivityScoped

@Suppress("unused")
@Module
abstract class UIModule {

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun splashScreenActivity(): SplashScreenActivity?

  @ActivityScoped @ContributesAndroidInjector abstract fun homeActivity(): HomeActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun configurationSettingsActivity(): ConfigurationSettingsActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun serverSettingsActivity(): ServerSettingsActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun settingsMenuActivity(): SettingsMenuActivity?

  @ActivityScoped @ContributesAndroidInjector abstract fun cardReaderActivity(): CardReaderActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun cardSummaryActivity(): CardSummaryActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun selectTicketsActivity(): SelectTicketsActivity?

  @ActivityScoped @ContributesAndroidInjector abstract fun checkoutActivity(): CheckoutActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun paymentValidatedActivity(): PaymentValidatedActivity?

  @ActivityScoped @ContributesAndroidInjector abstract fun chargeCardActivity(): ChargeActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun chargeResultActivity(): ChargeResultActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun personnalizationActivity(): PersonnalizationActivity?
}
