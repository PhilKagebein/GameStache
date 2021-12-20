package com.example.gamestache.ui.explore

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamestache.SpinnerAdapter
import com.example.gamestache.databinding.FragmentExploreBinding
import com.example.gamestache.models.explore_spinners.GameModesResponseItem
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem
import com.example.gamestache.models.explore_spinners.GenresResponseItem
import com.example.gamestache.models.explore_spinners.PlatformsResponseItem
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class ExploreFragment : Fragment() {

    private lateinit var exploreViewModel: ExploreViewModel
    private lateinit var binding: FragmentExploreBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initExploreViewModel()
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        binding.exploreviewmodel = exploreViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var platformSpinner: Spinner? = binding.spnPlatform
        var genreSpinner: Spinner? = binding.spnGenre
        var gameModesSpinner: Spinner? = binding.spnMultiplayer

        var searchText : RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        val exploreAdapter = GamesListSearchResultsAdapter(resources)

        binding.rvExplore.apply{
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = exploreAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
        exploreViewModel.getAccessToken()

        //TODO: HOW TO PERSIST STATE OF SPINNER SELECTION AFTER NAVIGATING BACK FROM INDIVIDUAL GAME FRAGMENT
        //TODO WALK THROUGH WITH KEVIN WHAT I DID WITH INITSPINNERS AND SETSPINNERONCLICK TO SEE IF THIS IS THE BEST WAY OF DOING IT
        exploreViewModel.currentPlatformListInRoomDB.observe(viewLifecycleOwner, { spinnerListFromRoom ->
             if (spinnerListFromRoom != null) {
                platformSpinner = platformSpinner?.let { initSpinners(it, spinnerListFromRoom, PLATFORM_SPINNER_PROMPT) }
                platformSpinner?.let { setSpinnerOnClick(it, "platform", savedInstanceState) }
            }
        })

        exploreViewModel.currentGenreListInRoomDB.observe(viewLifecycleOwner, { spinnerListFromRoom ->
            genreSpinner = genreSpinner?.let { initSpinners(it, spinnerListFromRoom, GENRE_SPINNER_PROMPT) }
            genreSpinner?.let {setSpinnerOnClick(it, "genre", savedInstanceState)}
        })
        exploreViewModel.currentGameModesListInRoomDB.observe(viewLifecycleOwner, { spinnerListFromRoom ->
            gameModesSpinner = gameModesSpinner?.let { initSpinners(it, spinnerListFromRoom, GAME_MODES_SPINNER_PROMPT) }
            gameModesSpinner?.let {setSpinnerOnClick(it, "gameMode", savedInstanceState)}
        })

        //TODO: TALK TO KEVIN ABOUT NOT ELIMINATING THIS BECAUSE I DON'T WANT THE SEARCH TO BE PERFORMED AUTOMATICALLY
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
        exploreViewModel.getPlatformsListFromRoom().observe(viewLifecycleOwner, { response ->
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
            gameModesSpinner?.setSelection(SPINNER_RESET_VALUE)
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

    private fun initSpinners(spinner: Spinner, spinnerListFromRoom: List<GenericSpinnerItem>, prompt: String): Spinner {
        val spinnerListWithPrompt = exploreViewModel.addPromptToSpinnerList(spinnerListFromRoom, prompt)
        val customAdapter = SpinnerAdapter(requireContext(), spinnerListWithPrompt)
        spinner.adapter = customAdapter
        return spinner

    }

    //TODO: MAKE THE STRINGS BELOW ENUMS
    private fun setSpinnerOnClick(spinner: Spinner, spinnerName: String, savedInstanceState: Bundle?) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                when (spinnerName) {
                    ExploreSpinners.PLATFORM_SPINNER.spinnerName -> binding.btnClearPlatformSpinner.visibility = exploreViewModel.setExploreSpinnersClearButtonVisibility(itemPosition)
                    ExploreSpinners.GENRE_SPINNER.spinnerName -> binding.btnClearGenreSpinner.visibility = exploreViewModel.setExploreSpinnersClearButtonVisibility(itemPosition)
                    ExploreSpinners.GAME_MODE_SPINNER.spinnerName -> binding.btnClearMultiplayerSpinner.visibility = exploreViewModel.setExploreSpinnersClearButtonVisibility(itemPosition)
                }
                if (itemPosition == 0) {
                    when (spinnerName) {
                        ExploreSpinners.PLATFORM_SPINNER.spinnerName -> exploreViewModel.platformText.postValue("")
                        ExploreSpinners.GENRE_SPINNER.spinnerName -> exploreViewModel.genreText.postValue("")
                        ExploreSpinners.GAME_MODE_SPINNER.spinnerName -> exploreViewModel.gameModesText.postValue("")
                    }
                } else {
                    when (spinnerName) {
                        ExploreSpinners.PLATFORM_SPINNER.spinnerName -> exploreViewModel.platformText.postValue("\"${spinner.getItemAtPosition(itemPosition)}\"")
                        ExploreSpinners.GENRE_SPINNER.spinnerName -> exploreViewModel.genreText.postValue("\"${spinner.getItemAtPosition(itemPosition)}\"")
                        ExploreSpinners.GAME_MODE_SPINNER.spinnerName -> exploreViewModel.gameModesText.postValue("\"${spinner.getItemAtPosition(itemPosition)}\"")
                    }
                }
            }
            override fun onNothingSelected(adapterview: AdapterView<*>?) {
            }
        }
    }

    private fun initExploreViewModel() {
             val factory = activity?.let { activity -> ExploreViewModelFactory(activity.application, resources) }
             exploreViewModel = factory?.let {customFactory -> ViewModelProvider(this, customFactory) }?.get(ExploreViewModel::class.java) as ExploreViewModel
    }

    companion object{
        const val SPINNER_RESET_VALUE = 0
        const val PLATFORM_SPINNER_PROMPT = "Select a platform"
        const val GENRE_SPINNER_PROMPT = "Select a genre"
        const val GAME_MODES_SPINNER_PROMPT = "Select multiplayer capabilities"
    }

}

enum class ExploreSpinners(val spinnerName: String) {
    PLATFORM_SPINNER("platform"),
    GENRE_SPINNER("genre"),
    GAME_MODE_SPINNER("gameMode")
}
