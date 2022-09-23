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
package org.calypsonet.keyple.demo.reload.remote.ui.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.calypsonet.keyple.demo.reload.remote.ui.activity.CardReaderActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.CardSummaryActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.ChargeActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.ChargeResultActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.CheckoutActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.ConfigurationSettingsActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.HomeActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.MainActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.PaymentValidatedActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.PersonnalizationActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.SelectTicketsActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.ServerSettingsActivity
import org.calypsonet.keyple.demo.reload.remote.ui.activity.SettingsMenuActivity
import org.calypsonet.keyple.demo.reload.remote.ui.di.scopes.ActivityScoped

@Suppress("unused")
@Module
abstract class UIModule {

  @ActivityScoped @ContributesAndroidInjector abstract fun splashScreenActivity(): MainActivity?

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
