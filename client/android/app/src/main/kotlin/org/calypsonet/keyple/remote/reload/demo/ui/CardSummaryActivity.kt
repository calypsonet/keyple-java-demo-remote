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
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_card_summary.animation
import kotlinx.android.synthetic.main.activity_card_summary.bigText
import kotlinx.android.synthetic.main.activity_card_summary.buyBtn
import kotlinx.android.synthetic.main.activity_card_summary.contentTitle
import kotlinx.android.synthetic.main.activity_card_summary.lastValidationContent
import kotlinx.android.synthetic.main.activity_card_summary.lastValidationList
import kotlinx.android.synthetic.main.activity_card_summary.smallDesc
import kotlinx.android.synthetic.main.activity_card_summary.titlesList
import org.calypsonet.keyple.remote.reload.demo.R
import org.calypsonet.keyple.remote.reload.demo.adapters.TitlesRecyclerAdapter
import org.calypsonet.keyple.remote.reload.demo.adapters.ValidationsRecyclerAdapter
import org.calypsonet.keyple.remote.reload.demo.data.model.CardReaderResponse
import org.calypsonet.keyple.remote.reload.demo.data.model.Status
import org.calypsonet.keyple.remote.reload.demo.setDivider

class CardSummaryActivity : AbstractDemoActivity() {

    private lateinit var validationLinearLayoutManager: LinearLayoutManager
    private lateinit var titleLinearLayoutManager: LinearLayoutManager
    private lateinit var validationsAdapter: ValidationsRecyclerAdapter
    private lateinit var titlesAdapter: TitlesRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_summary)

        val cardContent: CardReaderResponse = intent.getParcelableExtra(AbstractCardActivity.CARD_CONTENT)!!

        validationLinearLayoutManager = LinearLayoutManager(this)
        lastValidationList.layoutManager = validationLinearLayoutManager

        titleLinearLayoutManager = LinearLayoutManager(this)
        titlesList.layoutManager = titleLinearLayoutManager

        validationsAdapter = ValidationsRecyclerAdapter(cardContent.lastValidationsList)
        lastValidationList.adapter = validationsAdapter
        lastValidationList.setDivider(R.drawable.recycler_view_divider)

        titlesAdapter = TitlesRecyclerAdapter(cardContent.titlesList)
        titlesList.adapter = titlesAdapter

        when (cardContent.status) {
            Status.INVALID_CARD -> {
                animation.setAnimation("error_orange_anim.json")
                animation.playAnimation()
                bigText.setText(R.string.card_invalid_label)
                bigText.setTextColor(resources.getColor(R.color.orange))
                smallDesc.text = String.format(getString(R.string.card_invalid_desc), cardContent.cardType)
                smallDesc.setTextColor(resources.getColor(R.color.orange))
                buyBtn.visibility = View.INVISIBLE
                titlesList.visibility = View.GONE
                lastValidationContent.visibility = View.GONE
                contentTitle.visibility = View.GONE
            }
            Status.TICKETS_FOUND, Status.SUCCESS -> {
                titlesList.visibility = View.VISIBLE
                animation.visibility = View.GONE
                bigText.visibility = View.GONE
                smallDesc.visibility = View.INVISIBLE
                buyBtn.visibility = View.VISIBLE
                lastValidationContent.visibility = View.VISIBLE
                contentTitle.visibility = View.VISIBLE
            }
            Status.EMPTY_CARD -> {
                animation.setAnimation("error_anim.json")
                animation.playAnimation()
                bigText.text = getString(R.string.no_valid_label)
                bigText.setTextColor(resources.getColor(R.color.red))
                smallDesc.visibility = View.VISIBLE
                smallDesc.setTextColor(resources.getColor(R.color.red))
                smallDesc.text = getString(R.string.no_valid_desc)
                buyBtn.visibility = View.VISIBLE
                titlesList.visibility = View.GONE
                lastValidationContent.visibility = View.VISIBLE
                contentTitle.visibility = View.GONE
            }
            Status.ERROR -> {
                animation.setAnimation("error_anim.json")
                animation.playAnimation()
                if (cardContent.errorMessage != null)
                    bigText.text = cardContent.errorMessage
                else
                    bigText.setText(R.string.error_label)
                bigText.setTextColor(resources.getColor(R.color.red))
                smallDesc.visibility = View.INVISIBLE
                buyBtn.visibility = View.INVISIBLE
                titlesList.visibility = View.GONE
                lastValidationContent.visibility = View.GONE
                contentTitle.visibility = View.GONE
            }
            else -> {
                animation.setAnimation("error_anim.json")
                animation.playAnimation()
                bigText.setText(R.string.error_label)
                bigText.setTextColor(resources.getColor(R.color.red))
                smallDesc.visibility = View.INVISIBLE
                buyBtn.visibility = View.INVISIBLE
                titlesList.visibility = View.GONE
                lastValidationContent.visibility = View.GONE
                contentTitle.visibility = View.GONE
            }
        }
        animation.playAnimation()

        // Play sound
        val mp: MediaPlayer = MediaPlayer.create(this, R.raw.reading_sound)
        mp.start()
        buyBtn.setOnClickListener {
            val intent = Intent(this, SelectTicketsActivity::class.java)
            getIntent().getStringExtra(AbstractCardActivity.CARD_APPLICATION_NUMBER)?.let {
                intent.putExtra(AbstractCardActivity.CARD_APPLICATION_NUMBER, it)
            }
            startActivity(intent)
            this@CardSummaryActivity.finish()
        }
    }
}
