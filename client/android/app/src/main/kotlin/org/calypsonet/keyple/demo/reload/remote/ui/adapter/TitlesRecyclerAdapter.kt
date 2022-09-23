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
package org.calypsonet.keyple.demo.reload.remote.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.title_recycler_row.view.titleDescription
import kotlinx.android.synthetic.main.title_recycler_row.view.titleName
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.model.CardTitle
import org.calypsonet.keyple.demo.reload.remote.inflate

class TitlesRecyclerAdapter(private val titles: List<CardTitle>) :
    RecyclerView.Adapter<TitlesRecyclerAdapter.TitleHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TitleHolder {
    val inflatedView = parent.inflate(R.layout.title_recycler_row, false)
    return TitleHolder(inflatedView)
  }

  class TitleHolder(v: View) : RecyclerView.ViewHolder(v) {

    private var view: View = v
    private var title: CardTitle? = null

    fun bindItem(title: CardTitle) {
      this.title = title
      view.titleName.text = title.name
      view.titleDescription.text = title.description
    }
  }

  override fun getItemCount() = titles.size

  override fun onBindViewHolder(holder: TitleHolder, position: Int) {
    val titleItem = titles[position]
    holder.bindItem(titleItem)
  }
}
