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

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import org.calypsonet.keyple.demo.reload.remote.Application
import org.calypsonet.keyple.demo.reload.remote.di.scopes.AppScoped

@AppScoped
@Component(
    modules =
        [
            AppModule::class,
            UIModule::class,
            AndroidSupportInjectionModule::class,
            DataModule::class,
            RestModule::class,
            ReaderModule::class])
interface AppComponent : AndroidInjector<Application?> {
  @Component.Builder
  interface Builder {
    @BindsInstance fun application(application: Application): Builder

    fun build(): AppComponent
  }
}
