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
package org.calypsonet.keyple.demo.reload.remote.ui.cardsummary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.calypsonet.keyple.demo.reload.remote.data.model.CardTitle
import org.calypsonet.keyple.demo.reload.remote.databinding.TitleRecyclerRowBinding
import org.calypsonet.keyple.demo.reload.remote.inflate

class TitlesRecyclerAdapter(private val titles: List<CardTitle>) :
    RecyclerView.Adapter<TitlesRecyclerAdapter.TitleHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TitleHolder {
    val binding =
        TitleRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return TitleHolder(binding)
  }

  class TitleHolder(private val binding: TitleRecyclerRowBinding) :
      RecyclerView.ViewHolder(binding.root) {

    private var title: CardTitle? = null

    fun bindItem(title: CardTitle) {
      this.title = title
      binding.titleName.text = title.name
      binding.titleDescription.text = title.description
    }
  }

  override fun getItemCount() = titles.size

  override fun onBindViewHolder(holder: TitleHolder, position: Int) {
    val titleItem = titles[position]
    holder.bindItem(titleItem)
  }
}
