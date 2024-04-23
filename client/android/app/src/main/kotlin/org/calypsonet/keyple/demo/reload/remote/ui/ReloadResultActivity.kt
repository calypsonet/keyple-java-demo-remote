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
package org.calypsonet.keyple.demo.reload.remote.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import com.airbnb.lottie.LottieDrawable
import java.util.Timer
import java.util.TimerTask
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.model.Status
import org.calypsonet.keyple.demo.reload.remote.databinding.ActivityChargeResultBinding

class ReloadResultActivity : AbstractDemoActivity() {

  private val timer = Timer()
  private lateinit var activityChargeResultBinding: ActivityChargeResultBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityChargeResultBinding = ActivityChargeResultBinding.inflate(layoutInflater)
    toolbarBinding = activityChargeResultBinding.appBarLayout
    setContentView(activityChargeResultBinding.root)
    toolbarBinding.toolbarLogo.setImageResource(R.drawable.ic_logo_white)

    val status = Status.getStatus(intent.getStringExtra(STATUS))

    activityChargeResultBinding.tryBtn.setOnClickListener { onBackPressed() }
    activityChargeResultBinding.cancelBtn.setOnClickListener {
      val intent = Intent(this, HomeActivity::class.java)
      startActivity(intent)
    }

    when (status) {
      Status.LOADING -> {
        activityChargeResultBinding.animation.setAnimation("loading_anim.json")
        activityChargeResultBinding.animation.repeatCount = LottieDrawable.INFINITE
        activityChargeResultBinding.bigText.visibility = View.INVISIBLE
        activityChargeResultBinding.btnLayout.visibility = View.INVISIBLE
      }
      Status.SUCCESS -> {
        activityChargeResultBinding.mainBackground.setBackgroundColor(
            resources.getColor(R.color.green))
        activityChargeResultBinding.animation.setAnimation("tick_white.json")
        activityChargeResultBinding.animation.repeatCount = 0
        activityChargeResultBinding.animation.playAnimation()
        activityChargeResultBinding.bigText.setText(R.string.charging_success_label)
        activityChargeResultBinding.bigText.visibility = View.VISIBLE
        activityChargeResultBinding.btnLayout.visibility = View.INVISIBLE

        if (intent.getBooleanExtra(IS_PERSONALIZATION_RESULT, false)) {
          activityChargeResultBinding.bigText.setText(R.string.perso_success_label)
        } else {
          activityChargeResultBinding.bigText.setText(R.string.charging_success_label)
        }

        Intent(this, HomeActivity::class.java)
        timer.schedule(
            object : TimerTask() {
              override fun run() {
                runOnUiThread { this@ReloadResultActivity.finish() }
              }
            },
            RETURN_DELAY_MS.toLong())
      }
      else -> {
        activityChargeResultBinding.mainBackground.setBackgroundColor(
            resources.getColor(R.color.red))
        activityChargeResultBinding.animation.setAnimation("error_white.json")
        activityChargeResultBinding.animation.repeatCount = 0
        activityChargeResultBinding.animation.playAnimation()

        val message = intent.getStringExtra(MESSAGE)
        if (intent.getBooleanExtra(IS_PERSONALIZATION_RESULT, false)) {
          activityChargeResultBinding.bigText.setText(R.string.perso_failed_label)
          activityChargeResultBinding.bigText.append(":\n")
          activityChargeResultBinding.bigText.append(message)
        } else {
          activityChargeResultBinding.bigText.setText(R.string.transaction_cancelled_label)
          activityChargeResultBinding.bigText.append(":\n")
          activityChargeResultBinding.bigText.append(message)
        }
        activityChargeResultBinding.bigText.visibility = View.VISIBLE
        activityChargeResultBinding.btnLayout.visibility = View.VISIBLE
      }
    }

    // Play sound
    val mp: MediaPlayer = MediaPlayer.create(this, R.raw.reading_sound)
    mp.start()
  }

  override fun onPause() {
    super.onPause()
    timer.cancel()
  }

  companion object {
    private const val RETURN_DELAY_MS = 5000
    const val TICKETS_NUMBER = "ticketsNumber"
    const val STATUS = "status"
    const val MESSAGE = "message"
    const val IS_PERSONALIZATION_RESULT = "isPersonalizationResult"
  }
}
