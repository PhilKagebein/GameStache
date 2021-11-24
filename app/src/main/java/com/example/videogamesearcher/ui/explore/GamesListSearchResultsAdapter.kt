package com.example.videogamesearcher.ui.explore

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.videogamesearcher.R
import com.example.videogamesearcher.databinding.SearchResultsBinding
import com.example.videogamesearcher.models.search_results.SearchResultsResponseItem

class GamesListSearchResultsAdapter(private val resources: Resources):
    ListAdapter<SearchResultsResponseItem, GamesListSearchResultsAdapter.SearchResultsResponseViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsResponseViewHolder {
        val binding = SearchResultsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultsResponseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultsResponseViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
        holder.itemView.setOnClickListener {
            val action = ExploreFragmentDirections.actionExploreFragmentToIndividualGameFragment(currentItem.id, currentItem.name)
            Navigation.findNavController(holder.itemView).navigate(action)
        }
    }

    inner class SearchResultsResponseViewHolder(private val binding: SearchResultsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: SearchResultsResponseItem) {

            //BINDING THE IMAGE
            Glide.with(binding.gameArtThumbNail).load("${game.cover?.url}").placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.gameArtThumbNail)

            //BINDING THE TITLE
            binding.apply {
                tvGameTitle.text = game.name
            }
            //TODO TALK THROUGH WITH KEVIN THE ISSUE OF CONCACTENATING THE STRING TO DISPLAY FOR PLATFORMS, GENRES, ETC. SEEM LIKE I HAVE TO DO IT IN THE ADAPTER.
            //BINDING THE PLATFORM NAME
            if (game.platforms.isNullOrEmpty()) {
                binding.tvGamePlatform.text = PLATFORM_NULL_MESSAGE
            } else {
                for (i in game.platforms.indices) {
                    binding.tvGamePlatform.text = game.platforms[i]?.name
                }
            }

            //BINDING THE GENRE(S) NAME(S)
            if (game.genres.isNullOrEmpty()) {
                binding.tvGameGenre.text = GENRE_NULL_MESSAGE
            } else {
                for (i in game.genres.indices) {
                    binding.tvGameGenre.text = game.genres[i]?.name
                }
            }

            //BINDING THE GAME MODES
            if (game.game_modes.isNullOrEmpty()) {
                binding.tvGameModes.text = GAME_MODES_NULL_MESSAGE
            } else {
                for (i in game.game_modes.indices) {
                    binding.tvGameModes.text = game.game_modes[i]?.name
                }
            }
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<SearchResultsResponseItem>() {

        override fun areItemsTheSame(oldItem: SearchResultsResponseItem, newItem: SearchResultsResponseItem): Boolean {
           return oldItem.id == newItem.id
        }

        //TODO TALK TO KEVIN ABOUT HOW TO PROPERLY IMPLEMENT BELOW WHEN NEW VS. OLD GAME MODES MIGHT HAVE VARYING NUMBER OF ENTRIES
        override fun areContentsTheSame(oldItem: SearchResultsResponseItem, newItem: SearchResultsResponseItem): Boolean {
            return when {
                oldItem.id != newItem.id -> false
                oldItem.name != newItem.name -> false
                oldItem.game_modes != newItem.game_modes -> false
                oldItem.genres != newItem.genres -> false
                oldItem.platforms != newItem.platforms -> false
                oldItem.cover != newItem.cover -> false
                else -> true
            }
        }
    }

    companion object{
        const val PLATFORM_NULL_MESSAGE = "No platforms listed"
        const val GENRE_NULL_MESSAGE = "No genres listed"
        const val GAME_MODES_NULL_MESSAGE = "No multiplayer modes listed"
    }

}
