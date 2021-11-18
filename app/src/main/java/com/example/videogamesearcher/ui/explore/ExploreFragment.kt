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
import com.example.videogamesearcher.Constants.Companion.SPINNER_RESET_VALUE
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
        //ASK SCREWN ABOUT THIS DIFFERENT BINDING
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // @@@ktg code style thing: "self-documenting code" is a principle that basically means "your code
        // should read like English, as much as possible". I would probably name these variables more
        // along the lines of "platformSpinner" since in English we put adjectives before the noun
        // that they describe, so it feels more natural.
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

        // @@@ktg these bits that create spinners look fairly similar. It'll involve a bit of refactoring,
        // but you should be able to write a single function that you call into multiple times in order to initialize
        // each of your spinners. The function encapsulation I believe can start the line after .observe for each of these.

        //Creating the Spinners
        exploreViewModel.readPlatformsList.observe(viewLifecycleOwner, { platformItem ->
            val platformList = exploreViewModel.createPlatformsListFromRoom(platformItem)
            spnPlatform = spnPlatform?.let { spnPlatform -> initSpinners(spnPlatform, platformList) }
            spnPlatform?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                    binding.btnClearPlatformSpinner.visibility = exploreViewModel.setBtnClearPlatformSpinnerVisibility(itemPosition)
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
        exploreViewModel.readGenresList.observe(viewLifecycleOwner, { genresItem ->
            val genresList = exploreViewModel.createGenresListFromRoom(genresItem)
            spnGenre = spnGenre?.let { spnGenre -> initSpinners(spnGenre, genresList) }
            spnGenre?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                    binding.btnClearGenreSpinner.visibility = exploreViewModel.setBtnClearGenreSpinnerVisibility(itemPosition)
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
        exploreViewModel.readGameModesList.observe(viewLifecycleOwner, { gameModesItem ->
            val gameModesList = exploreViewModel.createGameModesListFromRoom(gameModesItem)
            spnMultiplayer = spnMultiplayer?.let {spnMultiplayer -> initSpinners(spnMultiplayer, gameModesList) }
            spnMultiplayer?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                    binding.btnClearMultiplayerSpinner.visibility = exploreViewModel.setBtnClearMultiplayerSpinnerVisibility(itemPosition)
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

        //@@@ktg don't need this - the only time you end up using searchText is when you call into
        // performGameSearch, which passes searchText as an arg to exploreViewModel.searchGames. Since
        // exploreViewModel is the owner of searchText(), there's no need to pass it in, just access
        // it from within that function.
        exploreViewModel.searchText().observe(viewLifecycleOwner, { text ->
            searchText = text
        })

        exploreViewModel.gamesList.observe(viewLifecycleOwner, { gamesList ->
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
        //@@@ktg see other comment in ViewModel about refactoring to use Repository differently
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

        //@@@ktg Constants that only apply to a single class usually live in the companion object
        // of that class
        binding.btnClearPlatformSpinner.setOnClickListener {
            spnPlatform?.setSelection(SPINNER_RESET_VALUE)
        }

        binding.btnClearGenreSpinner.setOnClickListener {
            spnGenre?.setSelection(SPINNER_RESET_VALUE)
        }

        binding.btnClearMultiplayerSpinner.setOnClickListener {
            spnMultiplayer?.setSelection(SPINNER_RESET_VALUE)
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

    private fun initExploreViewModel(){
             val factory = activity?.let { activity -> ExploreViewModelFactory(activity.application, resources) }
             exploreViewModel = factory?.let {customFactory -> ViewModelProvider(this, customFactory) }?.get(ExploreViewModel::class.java) as ExploreViewModel
    }

}