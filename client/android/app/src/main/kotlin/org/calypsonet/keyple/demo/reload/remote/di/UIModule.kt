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
package org.calypsonet.keyple.demo.reload.remote.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.calypsonet.keyple.demo.reload.remote.di.scopes.ActivityScoped
import org.calypsonet.keyple.demo.reload.remote.ui.CardReaderActivity
import org.calypsonet.keyple.demo.reload.remote.ui.CheckoutActivity
import org.calypsonet.keyple.demo.reload.remote.ui.ConfigurationSettingsActivity
import org.calypsonet.keyple.demo.reload.remote.ui.HomeActivity
import org.calypsonet.keyple.demo.reload.remote.ui.MainActivity
import org.calypsonet.keyple.demo.reload.remote.ui.PaymentValidatedActivity
import org.calypsonet.keyple.demo.reload.remote.ui.PersonalizationActivity
import org.calypsonet.keyple.demo.reload.remote.ui.ReloadActivity
import org.calypsonet.keyple.demo.reload.remote.ui.ReloadResultActivity
import org.calypsonet.keyple.demo.reload.remote.ui.SelectTicketsActivity
import org.calypsonet.keyple.demo.reload.remote.ui.ServerSettingsActivity
import org.calypsonet.keyple.demo.reload.remote.ui.SettingsMenuActivity
import org.calypsonet.keyple.demo.reload.remote.ui.cardsummary.CardSummaryActivity

@Suppress("unused")
@Module
abstract class UIModule {

  @ActivityScoped @ContributesAndroidInjector abstract fun mainActivity(): MainActivity?

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

  @ActivityScoped @ContributesAndroidInjector abstract fun chargeCardActivity(): ReloadActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun chargeResultActivity(): ReloadResultActivity?

  @ActivityScoped
  @ContributesAndroidInjector
  abstract fun personalizationActivity(): PersonalizationActivity?
}
