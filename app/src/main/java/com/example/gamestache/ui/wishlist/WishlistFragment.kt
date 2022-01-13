package com.example.gamestache.ui.wishlist

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamestache.R
import com.example.gamestache.databinding.FragmentWishlistBinding
import com.example.gamestache.massageDataForListAdapter
import com.example.gamestache.ui.explore.GamesListAdapterFragment
import com.example.gamestache.ui.explore.GamesListSearchResultsAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class WishlistFragment : Fragment() {

    private val wishlistViewModel: WishlistViewModel by viewModel()
    private lateinit var binding: FragmentWishlistBinding
    private val wishlistAdapter = GamesListSearchResultsAdapter(GamesListAdapterFragment.WISHLIST)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWishlistBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        binding.wishlistviewmodel = wishlistViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGamesListRecyclerView(wishlistAdapter)

        wishlistViewModel.pullWishlistFromDb()

        wishlistViewModel.wishlistFromDb.observe(viewLifecycleOwner, { wishlist ->
            binding.noWishlistYetTextView.visibility = wishlistViewModel.setNoWishlistTextViewVisibility(wishlist)
            binding.wishlistRecyclerView.visibility = wishlistViewModel.setWishlistRecyclerViewVisibility(wishlist)
            wishlistAdapter.submitList(wishlist)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchView = initOptionsMenu(menu, inflater)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            override fun onQueryTextChange(filterText: String?): Boolean {
                filterWishlist(filterText)
                return false
            }

            override fun onQueryTextSubmit(filterText: String?): Boolean {
                filterWishlist(filterText)
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
        binding.wishlistRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = favoritesAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
    }

    private fun filterWishlist(query: String?) {
        if (query != null ) {
            val filterQuery = "%$query%"

            wishlistViewModel.filterWishlist(filterQuery).observe(viewLifecycleOwner, { filteredFavoritesList ->
                filteredFavoritesList?.let {
                    val massagedFavoritesList = massageDataForListAdapter(filteredFavoritesList)
                    wishlistAdapter.submitList(massagedFavoritesList)
                }
            })
        }
    }

}