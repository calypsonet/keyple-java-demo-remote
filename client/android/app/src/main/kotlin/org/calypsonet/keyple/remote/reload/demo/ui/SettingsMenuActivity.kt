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
package org.calypsonet.keyple.remote.reload.demo.ui

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings_menu.*
import org.calypsonet.keyple.remote.reload.demo.BuildConfig
import org.calypsonet.keyple.remote.reload.demo.R

class SettingsMenuActivity : AbstractDemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_menu)

        serverBtn.setOnClickListener {
            val intent = Intent(this, ServerSettingsActivity::class.java)
            startActivity(intent)
        }

        configurationBtn.setOnClickListener {
            val intent = Intent(this, ConfigurationSettingsActivity::class.java)
            startActivity(intent)
        }

        personnalizationBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra(HomeActivity.CHOOSE_DEVICE_FOR_PERSO, true)
            startActivity(intent)
        }

        versionName.text = getString(R.string.version, BuildConfig.VERSION_NAME)
    }
}
