package com.example.videogamesearcher.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videogamesearcher.R
import com.example.videogamesearcher.databinding.FragmentExploreBinding
import com.example.videogamesearcher.repository.Repository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ExploreFragment : Fragment() {

    private lateinit var exploreViewModel: ExploreViewModel
    private var _binding: FragmentExploreBinding? = null
    private val repository = Repository()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initExploreViewModel()

        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        binding.exploreviewmodel = exploreViewModel
        binding.lifecycleOwner = this
        return binding.root
        //ASK SCREWN ABOUT THIS DIFFERENT BINDING
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var spnPlatform: Spinner? = binding.spnPlatform
        var spnGenre: Spinner? = binding.spnGenre
        var spnMultiplayer: Spinner? = binding.spnMultiplayer
        var searchText : RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        val exploreAdapter = GamesListSearchResultsAdapter(resources)

        binding.rvExplore.apply{
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = exploreAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
        exploreViewModel.getAccessToken()

        exploreViewModel.createPlatformsList().observe(viewLifecycleOwner, { platformsList ->
            //USE ROOM DB TO CACHE THE SPINNER LISTS
            spnPlatform = platformsList?.let { platforms -> spnPlatform?.let { spnPlatform -> initSpinners(spnPlatform, platforms) }}
            spnPlatform?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                    if (itemPosition == 0) {
                        exploreViewModel.platformText.postValue("")
                    } else {
                        exploreViewModel.platformText.postValue("platforms.name = \"${spnPlatform?.getItemAtPosition(itemPosition).toString()}\"")
                    }
                }

                override fun onNothingSelected(adapterview: AdapterView<*>?) {

                }
            }
        })

        exploreViewModel.createGenresList().observe(viewLifecycleOwner, { genresList ->
            spnGenre = genresList?.let { genres -> spnGenre?.let { spnGenre -> initSpinners(spnGenre, genres) } }
            spnGenre?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                    if (itemPosition == 0) {
                        exploreViewModel.genreText.postValue("")
                    } else {
                        exploreViewModel.genreText.postValue("genres.name = \"${spnGenre?.getItemAtPosition(itemPosition).toString()}\"")
                    }
                }

                override fun onNothingSelected(adapterview: AdapterView<*>?) {

                }
            }
        })

        exploreViewModel.createGameModesList().observe(viewLifecycleOwner, { gameModesList ->
            spnMultiplayer = gameModesList?.let { gameModes -> spnMultiplayer?.let { spnMultiplayer -> initSpinners(spnMultiplayer, gameModes) } }
            spnMultiplayer?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                    if (itemPosition == 0) {
                        exploreViewModel.gameModesText.postValue("")
                    } else {
                        exploreViewModel.gameModesText.postValue("game_modes.name = \"${spnMultiplayer?.getItemAtPosition(itemPosition).toString()}\"")
                    }
                }

                override fun onNothingSelected(adapterview: AdapterView<*>?) {

                }
            }
        })

        exploreViewModel.searchText().observe(viewLifecycleOwner, { text ->
            searchText = text
        })

        exploreViewModel.gamesList.observe(viewLifecycleOwner, { gamesList ->
            exploreAdapter.submitList(gamesList)
        })

        binding.btnExploreSearch.setOnClickListener {
            exploreViewModel.twitchAuthorization.value?.access_token?.let {twitchAccessToken ->
                exploreViewModel.searchGames(twitchAccessToken, searchText)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initSpinners(spinner: Spinner, strArray: MutableList<String>): Spinner {
        spinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_items, strArray)
        return spinner
    }

    private fun initExploreViewModel(){
        val factory = activity?.let { activity -> ExploreViewModelFactory(repository, activity.application, resources)}
        exploreViewModel = factory?.let {factory -> ViewModelProvider(this, factory) }?.get(ExploreViewModel::class.java) as ExploreViewModel
    }

}