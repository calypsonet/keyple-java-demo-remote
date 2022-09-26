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

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.calypsonet.keyple.demo.reload.remote.Application
import org.calypsonet.keyple.demo.reload.remote.di.scopes.AppScoped

@Suppress("unused")
@Module
class DataModule {

  @Provides
  @AppScoped
  fun getSharedPreferences(app: Application): SharedPreferences {
    return app.getSharedPreferences("Keyple-prefs", Context.MODE_PRIVATE)
  }
}
