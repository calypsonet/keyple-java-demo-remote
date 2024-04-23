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
import org.calypsonet.keyple.demo.common.model.type.PriorityCode
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.databinding.ActivitySelectTicketsBinding

class SelectTicketsActivity : AbstractDemoActivity() {
  private lateinit var activitySelectTicketsBinding: ActivitySelectTicketsBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    activitySelectTicketsBinding = ActivitySelectTicketsBinding.inflate(layoutInflater)
    toolbarBinding = activitySelectTicketsBinding.appBarLayout
    setContentView(activitySelectTicketsBinding.root)

    activitySelectTicketsBinding.ticket1Label.text =
        resources.getQuantityString(R.plurals.x_tickets, 1, 1)
    activitySelectTicketsBinding.ticket2Label.text =
        resources.getQuantityString(R.plurals.x_tickets, 2, 2)
    activitySelectTicketsBinding.ticket3Label.text =
        resources.getQuantityString(R.plurals.x_tickets, 3, 3)
    activitySelectTicketsBinding.ticket4Label.text =
        resources.getQuantityString(R.plurals.x_tickets, 4, 4)
    activitySelectTicketsBinding.ticket1Price.text = getString(R.string.ticket_price, 1)
    activitySelectTicketsBinding.ticket2Price.text = getString(R.string.ticket_price, 2)
    activitySelectTicketsBinding.ticket3Price.text = getString(R.string.ticket_price, 3)
    activitySelectTicketsBinding.ticket4Price.text = getString(R.string.ticket_price, 4)
    activitySelectTicketsBinding.seasonPassPrice.text = getString(R.string.ticket_price, 20)

    activitySelectTicketsBinding.ticket1Btn.setOnClickListener {
      startCheckoutActivity(PriorityCode.MULTI_TRIP, 1)
    }
    activitySelectTicketsBinding.ticket2Btn.setOnClickListener {
      startCheckoutActivity(PriorityCode.MULTI_TRIP, 2)
    }
    activitySelectTicketsBinding.ticket3Btn.setOnClickListener {
      startCheckoutActivity(PriorityCode.MULTI_TRIP, 3)
    }
    activitySelectTicketsBinding.ticket4Btn.setOnClickListener {
      startCheckoutActivity(PriorityCode.MULTI_TRIP, 4)
    }

    activitySelectTicketsBinding.seasonPassBtn.setOnClickListener {
      startCheckoutActivity(PriorityCode.SEASON_PASS)
    }
  }

  private fun startCheckoutActivity(priorityCode: PriorityCode, ticketNumber: Int? = null) {
    val intent = Intent(this, CheckoutActivity::class.java)
    intent.putExtra(SELECTED_TICKET_PRIORITY_CODE, priorityCode.key)
    if (ticketNumber != null) {
      intent.putExtra(TICKETS_NUMBER, ticketNumber)
    }
    getIntent().getStringExtra(AbstractCardActivity.CARD_APPLICATION_NUMBER)?.let {
      intent.putExtra(AbstractCardActivity.CARD_APPLICATION_NUMBER, it)
    }
    startActivity(intent)
    this@SelectTicketsActivity.finish()
  }

  companion object {
    const val SELECTED_TICKET_PRIORITY_CODE = "SELECTED_TICKET_PRIORITY_CODE"
    const val TICKETS_NUMBER = "TICKETS_NUMBER"
  }
}
