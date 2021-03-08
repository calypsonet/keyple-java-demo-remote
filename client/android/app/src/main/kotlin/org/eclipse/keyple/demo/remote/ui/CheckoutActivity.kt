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
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlinx.android.synthetic.main.activity_checkout.expiryValue
import kotlinx.android.synthetic.main.activity_checkout.selectionLabel
import kotlinx.android.synthetic.main.activity_checkout.selectionPrice
import kotlinx.android.synthetic.main.activity_checkout.validateBtn
import org.cna.keyple.demo.sale.data.model.type.PriorityCode
import org.eclipse.keyple.demo.remote.R

class CheckoutActivity : AbstractDemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val selectedTicketPriorityCode = PriorityCode.valueOf(intent.getByteExtra(SelectTicketsActivity.SELECTED_TICKET_PRIORITY_CODE, PriorityCode.MULTI_TRIP_TICKET.code))
        val ticketNumberCount: Int = intent.getIntExtra(SelectTicketsActivity.TICKETS_NUMBER, 0)

        if (selectedTicketPriorityCode == PriorityCode.SEASON_PASS) {
            selectionLabel.text =
                getString(R.string.season_pass_title)
            selectionPrice.text = getString(R.string.ticket_price, 20)
        } else {
            selectionLabel.text =
                resources.getQuantityString(
                    R.plurals.x_tickets,
                    ticketNumberCount,
                    ticketNumberCount
                )
            selectionPrice.text = getString(R.string.ticket_price, ticketNumberCount)
        }
        validateBtn.setOnClickListener {
            val intent = Intent(this, PaymentValidatedActivity::class.java)
            intent.putExtras(getIntent())
            startActivity(intent)
            this@CheckoutActivity.finish()
        }

        val now = Calendar.getInstance().time
        val sdf = SimpleDateFormat("MM/yy")
        expiryValue.text = sdf.format(now)
    }
}
