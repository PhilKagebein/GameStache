package com.example.videogamesearcher.ui.explore

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.videogamesearcher.R
import com.example.videogamesearcher.databinding.SearchResultsBinding
import com.example.videogamesearcher.models.search_results.SearchResultsResponseItem
import java.net.URI

class GamesListSearchResultsAdapter(private val reso: Resources):
    ListAdapter<SearchResultsResponseItem, GamesListSearchResultsAdapter.SearchResultsResponseViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsResponseViewHolder {
        val binding = SearchResultsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultsResponseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultsResponseViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class SearchResultsResponseViewHolder(private val binding: SearchResultsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: SearchResultsResponseItem) {
            var genreText = ""
            var gameModeText = ""
            var platformsText = ""

            //BINDING THE IMAGE
            if (game.cover == null) {
                Glide.with(binding.gameArtThumbNail).load("").placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.gameArtThumbNail)
            } else {
                val url = URI(game.cover.url)
                val segments = url.path.split("/")
                val lastSegment = segments[segments.size - 1]
                val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
                Glide.with(binding.gameArtThumbNail).load("https://images.igdb.com/igdb/image/upload/t_cover_big/${imageHash}.jpg").placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.gameArtThumbNail)
            }
            //BINDING THE TITLE
            binding.apply {
                tvGameTitle.text = game.name
            }
            //BINDING THE PLATFORM NAME
            if (game.platforms == null) {
                binding.tvGamePlatform.text = reso.getString(R.string.no_platform_null_error)
            } else {
                for (i in game.platforms.indices) {
                    if (i == (game.platforms.size-1)) {
                        platformsText = "${platformsText}${game.platforms[i].name}"
                    } else {
                        platformsText = "${platformsText}${game.platforms[i].name}, "
                    }
                }
                binding.tvGamePlatform.text = platformsText
            }
            //BINDING THE GENRE(S) NAME(S)
            if (game.genres == null) {
                binding.tvGameGenre.text = reso.getText(R.string.no_genre_null_error)
            } else {
                for (i in game.genres.indices) {
                    if (i == (game.genres.size-1)) {
                        genreText = "${genreText}${game.genres[i].name}"
                    } else {
                        genreText = "${genreText}${game.genres[i].name}, "
                    }
                }
                binding.tvGameGenre.text = genreText
            }
            //BINDING THE GAME MODES
            if (game.game_modes == null) {
                binding.tvGameModes.text = reso.getText(R.string.no_gamemodes_null_error)
            } else {
                for (i in game.game_modes.indices) {
                    if (i == (game.game_modes.size-1)) {
                        gameModeText = "${gameModeText}${game.game_modes[i].name}"
                    } else {
                        gameModeText = "${gameModeText}${game.game_modes[i].name}, "
                    }
                }
                binding.tvGameModes.text = gameModeText
            }
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<SearchResultsResponseItem>() {
        override fun areItemsTheSame(oldItem: SearchResultsResponseItem, newItem: SearchResultsResponseItem): Boolean {
           return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SearchResultsResponseItem, newItem: SearchResultsResponseItem): Boolean {
            return oldItem == newItem
        }
    }

}
