package com.stache.gamestache.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stache.gamestache.R
import com.stache.gamestache.databinding.SearchResultsBinding
import com.stache.gamestache.models.search_results.SearchResultsResponseItem
import com.stache.gamestache.ui.favorites.FavoritesFragmentDirections
import com.stache.gamestache.ui.wishlist.WishlistFragmentDirections

class GamesListSearchResultsAdapter(private val fragment: GamesListAdapterFragment):
    ListAdapter<SearchResultsResponseItem, GamesListSearchResultsAdapter.SearchResultsResponseViewHolder>(DiffCallback()) {

    private var action: NavDirections? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsResponseViewHolder {
        val binding = SearchResultsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultsResponseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultsResponseViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
        holder.itemView.setOnClickListener {
            when (fragment) {

                GamesListAdapterFragment.EXPLORE -> {
                    action = currentItem.id?.let { id ->
                        currentItem.name?.let { name ->
                            ExploreFragmentDirections.actionExploreFragmentToIndividualGameFragment(id, name)
                        }
                    }
                }

                GamesListAdapterFragment.FAVORITES -> {
                    action = currentItem.id?.let { id ->
                        currentItem.name?.let { name ->
                            FavoritesFragmentDirections.actionNavigationFavoritesToIndividualGameFragment(
                                id,
                                name
                            )
                        }
                    }
                }

                GamesListAdapterFragment.WISHLIST -> {
                    action = currentItem.id?.let { id ->
                        currentItem.name?.let { name ->
                            WishlistFragmentDirections.actionNavigationWishlistToIndividualGameFragment(
                                id,
                                name
                            )
                        }
                    }
                }

            }

            action?.let { action ->
                Navigation.findNavController(holder.itemView).navigate(action)
            }
        }
    }

    inner class SearchResultsResponseViewHolder(private val binding: SearchResultsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: SearchResultsResponseItem) {

            Glide.with(binding.gameArtThumbNail).load("${game.cover?.url}").placeholder(R.drawable.no_image).error(R.drawable.no_image).into(binding.gameArtThumbNail)

            binding.apply {
                tvGameTitle.text = game.name

                if (game.platformsToDisplay.isNullOrEmpty() || game.platformsToDisplay.isNullOrBlank()) {
                    tvGamePlatform.text = PLATFORM_NULL_MESSAGE
                } else {
                    tvGamePlatform.text = game.platformsToDisplay
                }

                if (game.genresToDisplay.isNullOrEmpty() || game.genresToDisplay.isNullOrBlank()) {
                    tvGameGenre.text = GENRE_NULL_MESSAGE
                } else {
                    tvGameGenre.text = game.genresToDisplay
                }

                if (game.gameModesToDisplay.isNullOrEmpty() || game.gameModesToDisplay.isNullOrBlank()) {
                    tvGameModes.text = GAME_MODES_NULL_MESSAGE
                } else {
                    tvGameModes.text = game.gameModesToDisplay
                }
            }

        }
    }

    class DiffCallback: DiffUtil.ItemCallback<SearchResultsResponseItem>() {

        override fun areItemsTheSame(oldItem: SearchResultsResponseItem, newItem: SearchResultsResponseItem): Boolean {
           return oldItem.id == newItem.id
        }

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

enum class GamesListAdapterFragment {
    FAVORITES,
    EXPLORE,
    WISHLIST
}
