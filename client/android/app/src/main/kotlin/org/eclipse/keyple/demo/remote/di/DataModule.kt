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

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import org.eclipse.keyple.demo.remote.Application
import org.eclipse.keyple.demo.remote.di.scopes.AppScoped

@Module
class DataModule {
    private val PREF_NAME = "Keyple-prefs"
    @Provides
    @AppScoped
    fun getSharedPreferences(app: Application): SharedPreferences {
        return app.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
