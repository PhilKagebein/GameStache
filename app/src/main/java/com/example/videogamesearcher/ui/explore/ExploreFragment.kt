package com.example.videogamesearcher.ui.explore

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videogamesearcher.SpinnerAdapter
import com.example.videogamesearcher.databinding.FragmentExploreBinding
import com.example.videogamesearcher.models.explore_spinners.GameModesResponseItem
import com.example.videogamesearcher.models.explore_spinners.GenresResponseItem
import com.example.videogamesearcher.models.explore_spinners.PlatformsResponseItem
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ExploreFragment : Fragment() {

    private lateinit var exploreViewModel: ExploreViewModel
    private var _binding: FragmentExploreBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initExploreViewModel()

        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        binding.exploreviewmodel = exploreViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var platformSpinner: Spinner? = binding.spnPlatform
        var genreSpinner: Spinner? = binding.spnGenre
        var multiplayerSpinner: Spinner? = binding.spnMultiplayer
        var searchText : RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        val exploreAdapter = GamesListSearchResultsAdapter(resources)

        binding.rvExplore.apply{
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = exploreAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
        exploreViewModel.getAccessToken()

        //Creating the Spinners
        exploreViewModel.readPlatformsList.observe(viewLifecycleOwner, { platformItem ->
            val platformList = exploreViewModel.createPlatformsListFromRoom(platformItem)
            platformSpinner = platformSpinner?.let { spnPlatform -> initSpinners(spnPlatform, platformList) }
            platformSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                    binding.btnClearPlatformSpinner.visibility = exploreViewModel.setExploreSpinnersClearButtonVisibility(itemPosition)
                    if (itemPosition == 0) {
                        exploreViewModel.platformText.postValue("")
                    } else {

                        exploreViewModel.platformText.postValue("platforms.name = \"${platformSpinner?.getItemAtPosition(itemPosition).toString()}\"")
                    }
                }

                override fun onNothingSelected(adapterview: AdapterView<*>?) {

                }
            }
        })
        exploreViewModel.readGenresList.observe(viewLifecycleOwner, { genresItem ->
            //I can put the two lines below into one function
            val genresList = exploreViewModel.createGenresListFromRoom(genresItem)
            genreSpinner = genreSpinner?.let { spnGenre -> initSpinners(spnGenre, genresList) }
            //But two I leave the ItemSelectedListener inside the observable? Do I put that in the spinner initialization function? Not in either?
            genreSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                    binding.btnClearGenreSpinner.visibility = exploreViewModel.setExploreSpinnersClearButtonVisibility(itemPosition)
                    if (itemPosition == 0) {
                        exploreViewModel.genreText.postValue("")
                    } else {
                        exploreViewModel.genreText.postValue("genres.name = \"${genreSpinner?.getItemAtPosition(itemPosition).toString()}\"")
                    }
                }

                override fun onNothingSelected(adapterview: AdapterView<*>?) {

                }
            }
        })
        exploreViewModel.readGameModesList.observe(viewLifecycleOwner, { gameModesItem ->
            val gameModesList = exploreViewModel.createGameModesListFromRoom(gameModesItem)
            multiplayerSpinner = multiplayerSpinner?.let { spnMultiplayer -> initSpinners(spnMultiplayer, gameModesList) }
            multiplayerSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                    binding.btnClearMultiplayerSpinner.visibility = exploreViewModel.setExploreSpinnersClearButtonVisibility(itemPosition)
                    if (itemPosition == 0) {
                        exploreViewModel.gameModesText.postValue("")
                    } else {
                        exploreViewModel.gameModesText.postValue("game_modes.name = \"${multiplayerSpinner?.getItemAtPosition(itemPosition).toString()}\"")
                    }
                }

                override fun onNothingSelected(adapterview: AdapterView<*>?) {

                }
            }
        })

        exploreViewModel.searchText().observe(viewLifecycleOwner, { text ->
            searchText = text
        })

        exploreViewModel.transformDataForListAdapter().observe(viewLifecycleOwner, { gamesList ->
            exploreAdapter.submitList(gamesList)
        })

        binding.etVideoGameSearch.setOnEditorActionListener {_, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performGameSearch(searchText)
            }
            true
        }

        binding.btnExploreSearch.setOnClickListener {
            performGameSearch(searchText)
        }

        //Storing Spinner data in Room
        exploreViewModel.platformsResponse().observe(viewLifecycleOwner, { response ->
            if (response != null) {
                for (i in response.indices) {
                    val item = PlatformsResponseItem(response[i].id, response[i].name)
                    exploreViewModel.addPlatformsListToRoom(item)
                }
            }
        })
        exploreViewModel.genresResponse().observe(viewLifecycleOwner, { response ->
            if (response != null) {
                for (i in response.indices) {
                    val item = GenresResponseItem(response[i].id, response[i].name)
                    exploreViewModel.addGenresListToRoom(item)
                }
            }
        })
        exploreViewModel.gameModesResponse().observe(viewLifecycleOwner, { response ->
            if (response != null) {
                for (i in response.indices) {
                    val item = GameModesResponseItem(response[i].id, response[i].name)
                    exploreViewModel.addGameModesListToRoom(item)
                }
            }
        })

        binding.btnClearPlatformSpinner.setOnClickListener {
            platformSpinner?.setSelection(SPINNER_RESET_VALUE)
        }

        binding.btnClearGenreSpinner.setOnClickListener {
            genreSpinner?.setSelection(SPINNER_RESET_VALUE)
        }

        binding.btnClearMultiplayerSpinner.setOnClickListener {
            multiplayerSpinner?.setSelection(SPINNER_RESET_VALUE)
        }

    }

    private fun performGameSearch(searchText: RequestBody) {
        collapseKeyboard()

        exploreViewModel.twitchAuthorization.value?.access_token?.let {twitchAccessToken ->
            exploreViewModel.searchGames(twitchAccessToken, searchText)
        }

    }

    private fun collapseKeyboard() {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initSpinners(spinner: Spinner, strArray: MutableList<String>): Spinner {
        val customAdapter = SpinnerAdapter(requireContext(), strArray)
        spinner.adapter = customAdapter
        return spinner
    }

    private fun initExploreViewModel() {
             val factory = activity?.let { activity -> ExploreViewModelFactory(activity.application, resources) }
             exploreViewModel = factory?.let {customFactory -> ViewModelProvider(this, customFactory) }?.get(ExploreViewModel::class.java) as ExploreViewModel
    }

    companion object{
        const val SPINNER_RESET_VALUE = 0
        const val BASIC_SEARCH_TEXT = "\nfields name, genres.name, platforms.name, game_modes.name, cover.url;\nlimit 100;\n"
    }

}
