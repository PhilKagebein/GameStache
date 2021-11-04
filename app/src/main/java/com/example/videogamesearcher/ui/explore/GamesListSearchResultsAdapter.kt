package com.example.videogamesearcher.ui.explore

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.videogamesearcher.R
import com.example.videogamesearcher.databinding.GamesListSearchResultsBinding
import com.example.videogamesearcher.models.GameNameResponse
import com.example.videogamesearcher.models.GameNameResponseItem

class GamesListSearchResultsAdapter(private val reso: Resources):
    ListAdapter<GameNameResponseItem, GamesListSearchResultsAdapter.GameNameResponseViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameNameResponseViewHolder {
        val binding = GamesListSearchResultsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameNameResponseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameNameResponseViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class GameNameResponseViewHolder(private val binding: GamesListSearchResultsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(game: GameNameResponseItem) {
            binding.apply {
                tvGamesListTitle.text = game.name
            }
            if (game.platforms == null) {
                binding.tvGamesListPlatform.text = reso.getString(R.string.no_platform_null_error)
            } else {
                binding.tvGamesListPlatform.text = game.platforms[0].name
            }

        }
    }

    class DiffCallback: DiffUtil.ItemCallback<GameNameResponseItem>() {
        override fun areItemsTheSame(oldItem: GameNameResponseItem, newItem: GameNameResponseItem): Boolean {
           return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GameNameResponseItem, newItem: GameNameResponseItem): Boolean {
            return oldItem == newItem
        }
    }

}
