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
import kotlinx.android.synthetic.main.activity_charge_result.animation
import kotlinx.android.synthetic.main.activity_charge_result.bigText
import kotlinx.android.synthetic.main.activity_charge_result.btnLayout
import kotlinx.android.synthetic.main.activity_charge_result.cancelBtn
import kotlinx.android.synthetic.main.activity_charge_result.mainBackground
import kotlinx.android.synthetic.main.activity_charge_result.tryBtn
import kotlinx.android.synthetic.main.toolbar.toolbarLogo
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.model.Status

class ChargeResultActivity : AbstractDemoActivity() {

  private val timer = Timer()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_charge_result)
    toolbarLogo.setImageResource(R.drawable.ic_logo_white)

    val status = Status.getStatus(intent.getStringExtra(STATUS))

    tryBtn.setOnClickListener { onBackPressed() }
    cancelBtn.setOnClickListener {
      val intent = Intent(this, HomeActivity::class.java)
      startActivity(intent)
    }

    when (status) {
      Status.LOADING -> {
        animation.setAnimation("loading_anim.json")
        animation.repeatCount = LottieDrawable.INFINITE
        bigText.visibility = View.INVISIBLE
        btnLayout.visibility = View.INVISIBLE
      }
      Status.SUCCESS -> {
        mainBackground.setBackgroundColor(resources.getColor(R.color.green))
        animation.setAnimation("tick_white.json")
        animation.repeatCount = 0
        animation.playAnimation()
        bigText.setText(R.string.charging_success_label)
        bigText.visibility = View.VISIBLE
        btnLayout.visibility = View.INVISIBLE

        if (intent.getBooleanExtra(IS_PERSONNALIZATION_RESULT, false)) {
          bigText.setText(R.string.perso_success_label)
        } else {
          bigText.setText(R.string.charging_success_label)
        }

        Intent(this, HomeActivity::class.java)
        timer.schedule(
            object : TimerTask() {
              override fun run() {
                runOnUiThread { this@ChargeResultActivity.finish() }
              }
            },
            RETURN_DELAY_MS.toLong())
      }
      else -> {
        mainBackground.setBackgroundColor(resources.getColor(R.color.red))
        animation.setAnimation("error_white.json")
        animation.repeatCount = 0
        animation.playAnimation()

        if (intent.getBooleanExtra(IS_PERSONNALIZATION_RESULT, false)) {
          bigText.setText(R.string.perso_failed_label)
        } else {
          bigText.setText(R.string.transaction_cancelled_label)
        }
        bigText.visibility = View.VISIBLE
        btnLayout.visibility = View.VISIBLE
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
    const val IS_PERSONNALIZATION_RESULT = "isPersonnalizationResult"
  }
}
