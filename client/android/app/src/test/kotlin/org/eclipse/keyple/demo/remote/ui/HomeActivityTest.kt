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

import android.os.Build
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.eclipse.keyple.demo.remote.R
import org.eclipse.keyple.demo.remote.data.model.DeviceEnum
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// import org.robolectric.RobolectricTestRunner

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class HomeActivityTest {

    @get:Rule
    val rule = activityScenarioRule<HomeActivity>()

    @Before
    fun init() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun clickContactLessSupport_shouldStartCardReaderActivity_withPrefData_withContactlessEnum() {
        val scenario = rule.scenario
        onView(withId(R.id.contactlessCardBtn)).perform(click())
        Intents.intended(IntentMatchers.hasComponent(CardReaderActivityTest::class.java.name))
        scenario.onActivity {
            Assert.assertNotNull(it.prefData.loadDeviceType())
            assert(DeviceEnum.getDeviceEnum(it.prefData.loadDeviceType()!!) == DeviceEnum.CONTACTLESS_CARD)
        }
    }

    @Test
    fun clickSimCard_shouldStartCardReaderActivity_withPrefData_withSimEnum() {
        val scenario = rule.scenario
        onView(withId(R.id.simCardBtn)).perform(click())
        Intents.intended(IntentMatchers.hasComponent(CardReaderActivityTest::class.java.name))
        scenario.onActivity {
            Assert.assertNotNull(it.prefData.loadDeviceType())
            assert(DeviceEnum.getDeviceEnum(it.prefData.loadDeviceType()!!) == DeviceEnum.SIM)
        }
    }

    @Test
    fun clickWearable_shouldStartCardReaderActivity_withPrefData_withWearableEnum() {
        val scenario = rule.scenario
        onView(withId(R.id.wearableBtn)).perform(click())
        Intents.intended(IntentMatchers.hasComponent(CardReaderActivityTest::class.java.name))
        scenario.onActivity {
            Assert.assertNotNull(it.prefData.loadDeviceType())
            assert(DeviceEnum.getDeviceEnum(it.prefData.loadDeviceType()!!) == DeviceEnum.WEARABLE)
        }
    }

    @Test
    fun clickEmbeddedSecureElement_shouldStartCardReaderActivity_withPrefData_withEmbeddedEnum() {
        val scenario = rule.scenario
        onView(withId(R.id.embeddedElemBtn)).perform(click())
        Intents.intended(IntentMatchers.hasComponent(CardReaderActivityTest::class.java.name))
        scenario.onActivity {
            Assert.assertNotNull(it.prefData.loadDeviceType())
            assert(DeviceEnum.getDeviceEnum(it.prefData.loadDeviceType()!!) == DeviceEnum.EMBEDDED)
        }
    }
}
