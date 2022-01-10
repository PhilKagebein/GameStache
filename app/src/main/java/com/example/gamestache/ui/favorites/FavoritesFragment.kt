package com.example.gamestache.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamestache.databinding.FragmentFavoritesBinding
import com.example.gamestache.ui.explore.GamesListAdapterFragment
import com.example.gamestache.ui.explore.GamesListSearchResultsAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModel()
    private lateinit var binding: FragmentFavoritesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        binding.favoritesviewmodel = favoritesViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val favoritesAdapter = GamesListSearchResultsAdapter(GamesListAdapterFragment.FAVORITES)
        setupGamesListRecyclerView(favoritesAdapter)

        favoritesViewModel.pullFavoritesListFromDb()

        favoritesViewModel.favoritesListFromDb.observe(viewLifecycleOwner, { favoritesList ->
                binding.noFavoritesYetTextView.visibility = favoritesViewModel.setNoFavoritesTextViewVisibility(favoritesList)
                binding.favoritesListRecyclerView.visibility = favoritesViewModel.setFavoritesListRecyclerViewVisibility(favoritesList)
                favoritesAdapter.submitList(favoritesList)
            })
        }

    private fun setupGamesListRecyclerView(favoritesAdapter: GamesListSearchResultsAdapter) {
        binding.favoritesListRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = favoritesAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
    }
}


