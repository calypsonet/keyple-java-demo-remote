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
package org.calypsonet.keyple.remote.reload.demo

import android.os.Build
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import org.calypsonet.keyple.remote.reload.demo.data.model.DeviceEnum
import org.calypsonet.keyple.remote.reload.demo.manager.KeypleManager
import org.calypsonet.keyple.remote.reload.demo.ui.CardReaderActivity
import org.eclipse.keyple.core.service.event.ObservableReader
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CardReaderActivityTest {

    @get:Rule
    val rule = activityScenarioRule<CardReaderActivity>()

    @Before
    fun init() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun readingAnEmptyContactLessSupport_shouldStartCardSummaryActivityWithEmptyMessage() {
        // TODO: TBC: How to mock AndroidNfc.enableReader() ?
        mockkObject(DeviceEnum)
        every { DeviceEnum.getDeviceEnum(any()) } returns DeviceEnum.CONTACTLESS_CARD

        val reader = mockk<AndroidNfcReader>()
        justRun { reader.activateProtocol(any(), any()) }
        justRun { reader.startCardDetection(ObservableReader.PollingMode.REPEATING) }

        val keypleServices = mockk<KeypleManager>()
        justRun { keypleServices.registerPlugin(any()) }
        every { keypleServices.getReader(any()) } returns reader
        every { keypleServices.getObservableReader(any()) } returns reader

        rule.scenario.onActivity {
        }
    }
}
