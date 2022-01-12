package com.example.gamestache.ui.favorites

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamestache.R
import com.example.gamestache.databinding.FragmentFavoritesBinding
import com.example.gamestache.ui.explore.GamesListAdapterFragment
import com.example.gamestache.ui.explore.GamesListSearchResultsAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


class FavoritesFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModel()
    private lateinit var binding: FragmentFavoritesBinding
    private val favoritesAdapter = GamesListSearchResultsAdapter(GamesListAdapterFragment.FAVORITES)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        binding.favoritesviewmodel = favoritesViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGamesListRecyclerView(favoritesAdapter)

        favoritesViewModel.pullFavoritesListFromDb()

        favoritesViewModel.favoritesListFromDb.observe(viewLifecycleOwner, { favoritesList ->
                binding.noFavoritesYetTextView.visibility = favoritesViewModel.setNoFavoritesTextViewVisibility(favoritesList)
                binding.favoritesListRecyclerView.visibility = favoritesViewModel.setFavoritesListRecyclerViewVisibility(favoritesList)
                favoritesAdapter.submitList(favoritesList)
            })
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val searchView = initOptionsMenu(menu, inflater)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(filterText: String?): Boolean {
                filterFavorites(filterText)
                return false
            }

            override fun onQueryTextChange(filterText: String?): Boolean {
                filterFavorites(filterText)
                return false
            }

        })
    }

    private fun initOptionsMenu(menu: Menu, inflater: MenuInflater): SearchView {
        inflater.inflate(R.menu.menu_item, menu)
        val item = menu.findItem(R.id.search_action)
        return item?.actionView as SearchView
    }

    private fun setupGamesListRecyclerView(favoritesAdapter: GamesListSearchResultsAdapter) {
        binding.favoritesListRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = favoritesAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
    }

    private fun filterFavorites(query: String?) {
        if (query != null ) {
            val filterQuery = "%$query%"

            favoritesViewModel.filterFavorites(filterQuery).observe(viewLifecycleOwner, { filteredFavoritesList ->
                filteredFavoritesList?.let {
                    val massagedFavoritesList = favoritesViewModel.massageDataForListAdapter(filteredFavoritesList)
                    favoritesAdapter.submitList(massagedFavoritesList)
                }
            })
        }
    }
}


