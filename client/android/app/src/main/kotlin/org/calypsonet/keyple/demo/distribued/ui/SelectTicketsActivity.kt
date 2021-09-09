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
package org.calypsonet.keyple.demo.distribued.ui

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_select_tickets.seasonPassBtn
import kotlinx.android.synthetic.main.activity_select_tickets.seasonPassPrice
import kotlinx.android.synthetic.main.activity_select_tickets.ticket1Btn
import kotlinx.android.synthetic.main.activity_select_tickets.ticket1Label
import kotlinx.android.synthetic.main.activity_select_tickets.ticket1Price
import kotlinx.android.synthetic.main.activity_select_tickets.ticket2Btn
import kotlinx.android.synthetic.main.activity_select_tickets.ticket2Label
import kotlinx.android.synthetic.main.activity_select_tickets.ticket2Price
import kotlinx.android.synthetic.main.activity_select_tickets.ticket3Btn
import kotlinx.android.synthetic.main.activity_select_tickets.ticket3Label
import kotlinx.android.synthetic.main.activity_select_tickets.ticket3Price
import kotlinx.android.synthetic.main.activity_select_tickets.ticket4Btn
import kotlinx.android.synthetic.main.activity_select_tickets.ticket4Label
import kotlinx.android.synthetic.main.activity_select_tickets.ticket4Price
import org.cna.keyple.demo.sale.data.model.type.PriorityCode
import org.eclipse.keyple.demo.distribued.R

class SelectTicketsActivity : AbstractDemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_tickets)

        ticket1Label.text = resources.getQuantityString(R.plurals.x_tickets, 1, 1)
        ticket2Label.text = resources.getQuantityString(R.plurals.x_tickets, 2, 2)
        ticket3Label.text = resources.getQuantityString(R.plurals.x_tickets, 3, 3)
        ticket4Label.text = resources.getQuantityString(R.plurals.x_tickets, 4, 4)
        ticket1Price.text = getString(R.string.ticket_price, 1)
        ticket2Price.text = getString(R.string.ticket_price, 2)
        ticket3Price.text = getString(R.string.ticket_price, 3)
        ticket4Price.text = getString(R.string.ticket_price, 4)
        seasonPassPrice.text = getString(R.string.ticket_price, 20)

        ticket1Btn.setOnClickListener {
            startCheckoutActivity(PriorityCode.MULTI_TRIP_TICKET, 1)
        }
        ticket2Btn.setOnClickListener {
            startCheckoutActivity(PriorityCode.MULTI_TRIP_TICKET, 2)
        }
        ticket3Btn.setOnClickListener {
            startCheckoutActivity(PriorityCode.MULTI_TRIP_TICKET, 3)
        }
        ticket4Btn.setOnClickListener {
            startCheckoutActivity(PriorityCode.MULTI_TRIP_TICKET, 4)
        }

        seasonPassBtn.setOnClickListener {
            startCheckoutActivity(PriorityCode.SEASON_PASS)
        }
    }

    private fun startCheckoutActivity(priorityCode: PriorityCode, ticketNumber: Int? = null) {
        val intent = Intent(this, CheckoutActivity::class.java)
        intent.putExtra(SELECTED_TICKET_PRIORITY_CODE, priorityCode.code)
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
