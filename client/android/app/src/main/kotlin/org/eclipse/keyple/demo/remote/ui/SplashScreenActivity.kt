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
package org.eclipse.keyple.demo.remote.ui

import android.content.Intent
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import java.util.Timer
import java.util.TimerTask
import org.eclipse.keyple.demo.remote.R

class SplashScreenActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Make sure this is before calling super.onCreate
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        /*
         * Wait for Wizway Device to be connected
         */
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (!isFinishing) {
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                }
            }
        }, SPLASH_MAX_DELAY_MS.toLong())
    }

    companion object {
        private const val SPLASH_MAX_DELAY_MS = 6000
    }
}
