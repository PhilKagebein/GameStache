package com.example.gamestache.ui.explore

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamestache.R
import com.example.gamestache.SpinnerAdapter
import com.example.gamestache.databinding.FragmentExploreBinding
import com.example.gamestache.isOnline
import com.example.gamestache.makeNoInternetToast
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExploreFragment : Fragment() {

    val exploreViewModel: ExploreViewModel by viewModel()
    private lateinit var binding: FragmentExploreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().installSplashScreen().apply {
            setKeepOnScreenCondition {
                exploreViewModel.isExploreFragmentLoading.value
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        binding.exploreviewmodel = exploreViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var platformSpinner: Spinner? = binding.platformSpinner
        var genreSpinner: Spinner? = binding.genreSpinner
        var gameModesSpinner: Spinner? = binding.multiplayerCapabilitiesSpinner

        var searchRequestBody: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        val exploreAdapter = GamesListSearchResultsAdapter(GamesListAdapterFragment.EXPLORE)
        val loadingDialog = Dialog(requireContext())

        binding.exploreResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = exploreAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
        exploreViewModel.getAuthToken(requireContext())

        exploreViewModel.currentPlatformListInDb.observe(viewLifecycleOwner, { spinnerListFromRoom ->
            platformSpinner = platformSpinner?.let { initSpinners(it, spinnerListFromRoom, resources.getString(R.string.platform_spinner_prompt), ExploreSpinners.PLATFORM_SPINNER
            ) }
            platformSpinner?.let { setSpinnerOnClick(it, "platform") }
            platformSpinner?.setSelection(exploreViewModel.platformSpinnerSelection)
            exploreViewModel.isExploreFragmentLoading.update { false }
        })

        exploreViewModel.currentGenreListInDb.observe(viewLifecycleOwner, { spinnerListFromRoom ->
            genreSpinner = genreSpinner?.let { initSpinners(it, spinnerListFromRoom, resources.getString(R.string.genre_spinner_prompt), ExploreSpinners.GENRE_SPINNER) }
            genreSpinner?.let { setSpinnerOnClick(it, "genre") }
            genreSpinner?.setSelection(exploreViewModel.genreSpinnerSelection)
        })

        exploreViewModel.currentGameModesListInDb.observe(viewLifecycleOwner, { spinnerListFromRoom ->
            gameModesSpinner = gameModesSpinner?.let { initSpinners(it, spinnerListFromRoom, resources.getString(R.string.game_modes_spinner_prompt), ExploreSpinners.GAME_MODE_SPINNER) }
            gameModesSpinner?.let { setSpinnerOnClick(it, "gameMode") }
            gameModesSpinner?.setSelection(exploreViewModel.gameModesSpinnerSelection)
        })

        exploreViewModel.searchText().observe(viewLifecycleOwner, { text ->
            searchRequestBody = text
        })

        exploreViewModel.transformDataForListAdapter().observe(viewLifecycleOwner, { gamesList ->
            if (gamesList.isNullOrEmpty()) {
                makeNoSearchResultsToast(requireContext(), resources).show()
            } else {
                exploreAdapter.submitList(gamesList)
            }
        })

        exploreViewModel.nameSearchText.observe(viewLifecycleOwner, { editText ->
            exploreViewModel.platformText.observe(viewLifecycleOwner, { platformText ->
                exploreViewModel.genreText.observe(viewLifecycleOwner, {genreText ->
                    exploreViewModel.gameModesText.observe(viewLifecycleOwner, { gameModeText ->

                        binding.exploreSearchFieldEditText.setOnEditorActionListener { _, actionId, _ ->
                            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                checkForEmptySearch(editText, platformText, genreText, gameModeText, searchRequestBody)
                            }
                            true
                        }

                        binding.exploreSearchButton.setOnClickListener {
                            checkForEmptySearch(editText, platformText, genreText, gameModeText, searchRequestBody)
                        }

                    })
                })
            })
        })

        binding.clearPlatformSpinnerButton.setOnClickListener {
            platformSpinner?.setSelection(SPINNER_RESET_VALUE)
        }

        binding.clearGenreSpinnerButton.setOnClickListener {
            genreSpinner?.setSelection(SPINNER_RESET_VALUE)
        }

        binding.clearMultiplayerCapabilitiesSpinnerButton.setOnClickListener {
            gameModesSpinner?.setSelection(SPINNER_RESET_VALUE)
        }

        binding.clearEditTextButton.setOnClickListener {
            exploreViewModel.nameSearchText.postValue("")
        }

        exploreViewModel.nameSearchText.observe(viewLifecycleOwner, { nameSearchText ->
            binding.clearEditTextButton.visibility =
                exploreViewModel.clearEditTextField(nameSearchText)
        })

        exploreViewModel.progressBarIsVisible.observe(viewLifecycleOwner, { progressBarVisibility ->
            if (progressBarVisibility) {
                loadingDialog.setContentView(R.layout.loading_dialog)
                loadingDialog.show()
            } else {
                loadingDialog.dismiss()
            }
        })

        exploreViewModel.twitchAuthorization.observe(viewLifecycleOwner, { twitchAuth ->
            twitchAuth?.let {
                if (isOnline(requireContext())) {
                    exploreViewModel.updateSpinnerListsFromApi(twitchAuth)
                } else {
                    Log.i(TWITCH_AUTH_LOG_TAG, IS_OFFLINE_LOG_TEXT)
                    makeNoInternetToast(requireContext(), resources).show()
                }
            } ?: run {
                Log.i(TWITCH_AUTH_LOG_TAG, TWITCH_AUTH_NULL_LOG_TEXT)
                makeNoInternetToast(requireContext(), resources).show()
            }
        })
    }

    private fun checkForEmptySearch(editText: String, platformText: String, genreText: String, gameModeText: String, searchRequestBody: RequestBody) {
        if (editText.isBlank() && platformText.isBlank() && genreText.isBlank() && gameModeText.isBlank()) {
            Toast.makeText(requireContext(),
                getString(R.string.blank_search_attempt_error_message),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            binding.exploreSearchFieldEditText.clearFocus()
            performGameSearch(searchRequestBody)
        }
    }

    private fun performGameSearch(searchRequestBody: RequestBody) {
        collapseKeyboard()

        exploreViewModel.twitchAuthorization.value?.access_token?.let { twitchAuthToken ->
            if (isOnline(requireContext())) {
                exploreViewModel.searchForGames(twitchAuthToken, searchRequestBody, requireContext())
            } else {
                Log.i(PERFORM_GAME_SEARCH_LOG_TAG, IS_OFFLINE_LOG_TEXT)
                makeNoInternetToast(requireContext(), resources).show()
            }
        } ?: run {
            exploreViewModel.getAuthToken(requireContext())
        }

    }

    private fun collapseKeyboard() {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
    }

    private fun initSpinners(spinner: Spinner, spinnerListFromRoom: MutableList<GenericSpinnerItem>, prompt: String, spinnerType: ExploreSpinners): Spinner {
        val spinnerListWithPrompt = exploreViewModel.addPromptToSpinnerList(spinnerListFromRoom, prompt, spinnerType)
        val customAdapter = SpinnerAdapter(requireContext(), spinnerListWithPrompt)
        spinner.adapter = customAdapter
        return spinner

    }

    private fun setSpinnerOnClick(spinner: Spinner, spinnerName: String) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
                when (spinnerName) {
                    ExploreSpinners.PLATFORM_SPINNER.spinnerName -> {
                        binding.clearPlatformSpinnerButton.visibility = exploreViewModel.setExploreSpinnersClearButtonVisibility(itemPosition)
                        exploreViewModel.platformSpinnerSelection = itemPosition
                    }
                    ExploreSpinners.GENRE_SPINNER.spinnerName -> {
                        binding.clearGenreSpinnerButton.visibility = exploreViewModel.setExploreSpinnersClearButtonVisibility(itemPosition)
                        exploreViewModel.genreSpinnerSelection = itemPosition
                    }
                    ExploreSpinners.GAME_MODE_SPINNER.spinnerName -> {
                        binding.clearMultiplayerCapabilitiesSpinnerButton.visibility = exploreViewModel.setExploreSpinnersClearButtonVisibility(itemPosition)
                        exploreViewModel.gameModesSpinnerSelection = itemPosition
                    }
                }
                if (itemPosition == 0) {
                    when (spinnerName) {
                        ExploreSpinners.PLATFORM_SPINNER.spinnerName -> exploreViewModel.platformText.postValue(resources.getString(R.string.empty))
                        ExploreSpinners.GENRE_SPINNER.spinnerName -> exploreViewModel.genreText.postValue(resources.getString(R.string.empty))
                        ExploreSpinners.GAME_MODE_SPINNER.spinnerName -> exploreViewModel.gameModesText.postValue(resources.getString(R.string.empty))
                    }
                } else {
                    when (spinnerName) {
                        ExploreSpinners.PLATFORM_SPINNER.spinnerName -> exploreViewModel.platformText.postValue(makeSpinnerTextForAPICall(spinner.getItemAtPosition(itemPosition).toString()))
                        ExploreSpinners.GENRE_SPINNER.spinnerName -> exploreViewModel.genreText.postValue(makeSpinnerTextForAPICall(spinner.getItemAtPosition(itemPosition).toString()))
                        ExploreSpinners.GAME_MODE_SPINNER.spinnerName -> exploreViewModel.gameModesText.postValue(makeSpinnerTextForAPICall(spinner.getItemAtPosition(itemPosition).toString()))
                    }
                }
            }

            override fun onNothingSelected(adapterview: AdapterView<*>?) {
            }
        }
    }

    companion object {
        const val SPINNER_RESET_VALUE = 0
        const val PERFORM_GAME_SEARCH_LOG_TAG = "ExploreFragment - performGameSearch"
        const val IS_OFFLINE_LOG_TEXT = "No internet connection from isOnline"
        const val TWITCH_AUTH_LOG_TAG = "ExploreFragment - twitchAuth"
        const val TWITCH_AUTH_NULL_LOG_TEXT = "TwitchAuth Null"
        fun makeNoSearchResultsToast(context: Context, resources: Resources): Toast = Toast.makeText(context, resources.getString(R.string.no_search_results_found_toast), Toast.LENGTH_SHORT)
        fun makeSpinnerTextForAPICall(text: String) = "\"$text\""
    }

    enum class ExploreSpinners(val spinnerName: String) {
        PLATFORM_SPINNER("platform"),
        GENRE_SPINNER("genre"),
        GAME_MODE_SPINNER("gameMode")
    }
}

