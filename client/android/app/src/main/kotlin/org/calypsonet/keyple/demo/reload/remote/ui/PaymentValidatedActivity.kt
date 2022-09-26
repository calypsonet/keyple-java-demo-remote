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
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_payment_validated.animation
import kotlinx.android.synthetic.main.activity_payment_validated.chargeBtn
import org.calypsonet.keyple.demo.reload.remote.R

class PaymentValidatedActivity : AbstractDemoActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_payment_validated)
    chargeBtn.text = getString(R.string.load_card)
    chargeBtn.setOnClickListener {
      val intent = Intent(this, ChargeActivity::class.java)
      intent.putExtras(getIntent())
      startActivity(intent)
      this@PaymentValidatedActivity.finish()
    }
  }

  override fun onResume() {
    super.onResume()
    animation.setAnimation("tick_anim.json")
    animation.repeatCount = 0
    animation.playAnimation()
  }
}
