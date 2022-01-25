package com.example.gamestache.ui.individual_game

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.gamestache.MainActivity
import com.example.gamestache.R
import com.example.gamestache.SpinnerAdapter
import com.example.gamestache.databinding.IndividualGameFragmentBinding
import com.example.gamestache.makeNoInternetToast
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem
import com.example.gamestache.models.individual_game.MultiplayerModesItem
import com.example.gamestache.models.individual_game.ReleaseDate
import com.example.gamestache.models.individual_game.SimilarGame
import com.example.gamestache.ui.explore.GamesListAdapterFragment
import kotlinx.android.synthetic.main.individual_game_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class IndividualGameFragment : Fragment() {

    private val args: IndividualGameFragmentArgs by navArgs()
    val individualGameViewModel: IndividualGameViewModel by viewModel()
    private lateinit var binding: IndividualGameFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = IndividualGameFragmentBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).supportActionBar?.title = args.gameName

        binding.individualGameTitle.text = args.gameName

        binding.individualgameviewmodel = individualGameViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingDialog = Dialog(requireContext())
        loadingDialog.setContentView(R.layout.loading_dialog)
        loadingDialog.show()

        var summaryText: String? = ""
        var imageURL = ""
        val releaseRegionSpinner = binding.releasesByRegionSpinner
        val multiplayerOnPlatformSpinner = binding.multiplayerOnPlatformSpinner
        var count = 0
        val similarGamesTextViews = mutableListOf<TextView>()

        individualGameViewModel.gameId.postValue(args.gameId)
        individualGameViewModel.getAuthToken(requireContext())

        individualGameViewModel.progressBarIsVisible.observe(viewLifecycleOwner, { progressBarStatus ->

            if (progressBarStatus) {
                loadingDialog.show()
            } else {
                object : CountDownTimer(COUNTDOWN_TIMER_DELAY, COUNTDOWN_TIMER_INTERVAL) {
                    override fun onFinish() {
                        loadingDialog.dismiss()
                    }

                    override fun onTick(p0: Long) {
                    }

                }.start()
            }

        })

        setInitialMultiPlayerCapabilitiesVisibility()

        individualGameViewModel.glideURL.observe(viewLifecycleOwner, { url ->
            imageURL = url
            Glide.with(this)
                .load(url)
                .placeholder(R.color.transparent)
                .error(R.color.transparent)
                .into(binding.individualGameArt)
        })

        binding.individualGameArt.setOnClickListener {
            val artDialog = ArtDialog(imageURL)
            artDialog.show(requireActivity().supportFragmentManager, "ArtDialog")
        }

        binding.summaryCardView.setOnClickListener {

            val arrowButtonBackGroundResource = individualGameViewModel.determineDropDownButtonOrientation(binding.summaryText.visibility)
            binding.summaryCardViewDropDownArrow.setBackgroundResource(arrowButtonBackGroundResource)

            binding.summaryText.visibility = individualGameViewModel.changeCardViewVisibility(binding.summaryText.visibility)

        }

        individualGameViewModel.summaryText.observe(viewLifecycleOwner, { liveSummaryText ->
            summaryText = liveSummaryText
        })

        binding.summaryText.setOnClickListener {
            val gameSummaryDialog = summaryText?.let { summaryText -> GameSummaryDialog(summaryText) }
            gameSummaryDialog?.show(requireActivity().supportFragmentManager, "GameSummaryDialog")
        }

        individualGameViewModel.regionsList.observe(viewLifecycleOwner, { regionsList ->
            individualGameViewModel.releaseDatesList.observe(viewLifecycleOwner, { releaseDates ->
                if (regionsList.isNullOrEmpty() || releaseDates.isNullOrEmpty()) {
                    binding.releaseRegionsCardView.visibility = GONE
                } else {
                    val spinner = initSpinner(regionsList, releaseRegionSpinner)

                    setReleaseRegionSpinnerSelection(spinner, regionsList)
                    setOnClickForReleaseRegionSpinner(releaseDates, releaseRegionSpinner)
                }
            })
        })

        individualGameViewModel.platformsListForMultiplayerModesSpinner.observe(viewLifecycleOwner, { platformListForSpinner ->
            individualGameViewModel.platformListFromDb.observe(viewLifecycleOwner, { platformListFromDb ->
                individualGameViewModel.multiplayerModesList.observe(viewLifecycleOwner, { multiplayerModesList ->

                    if (multiplayerModesList.isNullOrEmpty()) {
                        binding.multiplayerCapabilitiesLinearLayout.visibility = GONE
                        binding.multiplayerCapabilitiesSectionTitleAndSpinnerLinearLayout.visibility = GONE
                    }
                    val spinner = initSpinner(platformListForSpinner, multiplayerOnPlatformSpinner)

                    setOnClickForMultiplayerPlatformSpinner(multiplayerModesList, spinner, platformListFromDb)
                })
            })
        })

        binding.releaseRegionsCardView.setOnClickListener {

            val arrowButtonBackGroundResource = individualGameViewModel.determineDropDownButtonOrientation(binding.releasesByRegionSpinner.visibility)
            binding.releasesByRegionDropDownButton.setBackgroundResource(arrowButtonBackGroundResource)

            binding.releasesByRegionSpinner.visibility = individualGameViewModel.changeCardViewVisibility(binding.releasesByRegionSpinner.visibility)
            binding.releaseRegionInformationTextView.visibility = individualGameViewModel.changeCardViewVisibility(binding.releaseRegionInformationTextView.visibility)
        }

        binding.genresAndPerspectivesCardView.setOnClickListener {

            val arrowButtonBackGroundResource = individualGameViewModel.determineDropDownButtonOrientation(binding.playerPerspectivesTextView.visibility)
            binding.playerPerspectivesAndGenresDropDownButton.setBackgroundResource(arrowButtonBackGroundResource)

            binding.genresTextView.visibility = individualGameViewModel.changeCardViewVisibility(binding.genresTextView.visibility)
            binding.playerPerspectivesTextView.visibility = individualGameViewModel.changeCardViewVisibility(binding.playerPerspectivesTextView.visibility)

        }

        individualGameViewModel.multiplayerModesList.observe(viewLifecycleOwner, { multiplayerModes ->
            binding.gameModesCardView.setOnClickListener {

                val arrowButtonBackGroundResource = individualGameViewModel.determineDropDownButtonOrientation(binding.gameModesTextView.visibility)
                binding.gameModesDropDownButton.setBackgroundResource(arrowButtonBackGroundResource)

                binding.gameModesTextView.visibility = individualGameViewModel.changeCardViewVisibility(binding.gameModesTextView.visibility)
                binding.multiplayerCapabilitiesSectionTitleAndSpinnerLinearLayout.visibility = individualGameViewModel.getMultiplayerTitleAndSpinnerVisibility( binding.multiplayerCapabilitiesSectionTitleAndSpinnerLinearLayout.visibility, multiplayerModes)
                binding.multiplayerCapabilitiesLinearLayout.visibility = individualGameViewModel.changeCardViewVisibility(binding.multiplayerCapabilitiesLinearLayout.visibility)

            }
        })

        binding.similarGamesCardView.setOnClickListener {
            val arrowButtonBackGroundResource = individualGameViewModel.determineDropDownButtonOrientation(similarGamesTextViews[0].visibility)
            binding.similarGamesDropDownButton.setBackgroundResource(arrowButtonBackGroundResource)

            for (textView in similarGamesTextViews) {
                textView.visibility = individualGameViewModel.changeCardViewVisibility(textView.visibility)
            }
        }

        individualGameViewModel.similarGamesList.observe(viewLifecycleOwner, { similarGamesList ->
            populateSimilarGamesTextViews(similarGamesList, count, similarGamesTextViews)
            count++
            formatSimilarGamesTextViews(similarGamesTextViews)
            setSimilarGameOnClickListener(similarGamesList, similarGamesTextViews)
        })

        individualGameViewModel.getIndividualGameData().observe(viewLifecycleOwner, { gameData ->

            if (gameData.isNullOrEmpty()) {
                makeNoInternetToast(requireContext(), resources).show()
            }

            binding.favoritesButton.setOnClickListener {
                binding.favoritesButton.text = individualGameViewModel.onFavoriteOrWishlistButtonPush(binding.favoritesButton.text.toString(), gameData,
                    GamesListAdapterFragment.FAVORITES
                )
            }

            binding.wishListButton.setOnClickListener {
                binding.wishListButton.text = individualGameViewModel.onFavoriteOrWishlistButtonPush(binding.wishListButton.text.toString(), gameData,
                    GamesListAdapterFragment.WISHLIST
                )
            }
        })

    }

    private fun setSimilarGameOnClickListener(similarGamesList: List<SimilarGame?>, similarGamesTextViews: MutableList<TextView>) {
        for (textView in similarGamesTextViews.indices) {
            similarGamesTextViews[textView].setOnClickListener {
                similarGamesList[textView]?.id?.let { id ->
                    similarGamesList[textView]?.name?.let { name ->
                        val action = IndividualGameFragmentDirections.actionIndividualGameFragmentSelf(id, name)
                        view?.let { view -> Navigation.findNavController(view).navigate(action) }
                    }
                }
            }
        }
    }

    private fun populateSimilarGamesTextViews(similarGamesList: List<SimilarGame?>, count: Int, similarGamesTextViews: MutableList<TextView>) {
        if (count == 0) {
            for ( game in similarGamesList.indices ) {
                val similarGameTextView = TextView(context)
                similarGameTextView.text = getString(R.string.similar_game, similarGamesList[game]?.name)
                binding.similarGamesLinearLayout.addView(similarGameTextView)
                similarGamesTextViews.add(similarGameTextView)
            }
        }
    }

    private fun formatSimilarGamesTextViews(similarGamesTextViews: MutableList<TextView>) {
        val typedValue = TypedValue()
        val theme = context?.theme
        theme?.resolveAttribute(R.attr.textColor, typedValue, true)

        for (textView in similarGamesTextViews) {
            textView.apply {
                textSize = SIMILAR_GAME_TEXT_SIZE
                setPadding(SIMILAR_GAME_LEFT_PADDING, SIMILAR_GAME_TOP_PADDING, SIMILAR_GAME_RIGHT_PADDING, SIMILAR_GAME_BOTTOM_PADDING)
                setTextColor(typedValue.data)
                textAlignment = left
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textView.visibility = GONE
            }
        }
    }

    private fun setReleaseRegionSpinnerSelection(releaseRegionsSpinner: Spinner, regionsList: MutableList<String?>) {
        if (regionsList.contains(RELEASE_REGION_SPINNER_REGION_DEFAULT)) {
            releaseRegionsSpinner.setSelection(regionsList.indexOf(RELEASE_REGION_SPINNER_REGION_DEFAULT))
        } else if (regionsList.size >= 2){
            releaseRegionsSpinner.setSelection(1)
        } else {
            releaseRegionsSpinner.setSelection(0)
        }
    }

    private fun initSpinner(itemsList: MutableList<String?>, spinner: Spinner): Spinner {
        val customAdapter = SpinnerAdapter(requireContext(), itemsList)
        spinner.adapter = customAdapter

        return spinner
    }

    private fun setOnClickForReleaseRegionSpinner(releaseDates: List<ReleaseDate?>?, releaseRegionsSpinner: Spinner) {

        releaseRegionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowID: Long) {
                val releaseInformationText = releaseDates?.let {
                    individualGameViewModel.getReleaseInformationText(it, releaseRegionsSpinner.getItemAtPosition(itemPosition).toString())
                } ?: throw NullPointerException()
                individualGameViewModel.releaseInformationText.postValue(releaseInformationText)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun getMultiplayerModeItemVisibility(itemSelected: Int): Int {
        if (itemSelected == 0) return GONE
        else return VISIBLE
    }

    private fun setOnClickForMultiplayerPlatformSpinner(multiplayerModes: List<MultiplayerModesItem?>?, spinner: Spinner, platformListFromDb: List<GenericSpinnerItem>) {
        multiplayerOnPlatformSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {

                    getMultiplayerModeItemVisibility(itemPosition).apply {
                        binding.coopCapabilitiesText.visibility = this
                        binding.coopCapabilitiesTitleTextView.visibility = this
                        binding.offlineCapabilities.visibility = this
                        binding.offlineCapabilitiesTitleTextView.visibility = this
                        binding.onlineCapabilities.visibility = this
                        binding.onlineCapabilitiesTitleTextView.visibility = this
                    }

                    val coopCapabilitiesText: String =
                        individualGameViewModel.getCoopCapabilitiesText(multiplayerModes,
                            spinner.getItemAtPosition(itemPosition).toString(),
                            platformListFromDb
                        )
                    individualGameViewModel.coopCapabilitiesText.postValue(coopCapabilitiesText)

                    val offlineCapabilitiesText: String =
                        individualGameViewModel.getOfflineCapabilitiesText(
                            multiplayerModes,
                            spinner.getItemAtPosition(itemPosition).toString(),
                            platformListFromDb
                        )
                    individualGameViewModel.offlineCapabilitiesText.postValue(
                        offlineCapabilitiesText
                    )

                    val onlineCapabilitiesText: String =
                        individualGameViewModel.getOnlineCapabilitiesText(
                            multiplayerModes,
                            spinner.getItemAtPosition(itemPosition).toString(),
                            platformListFromDb
                        )
                    individualGameViewModel.onlineCapabilitiesText.postValue(onlineCapabilitiesText)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
    }

    private fun setInitialMultiPlayerCapabilitiesVisibility() {
        binding.gameModesTextView.visibility = INITIAL_MULTIPLAYER_CAPABILITY_VISIBILITY
        binding.multiplayerCapabilitiesSectionTitleAndSpinnerLinearLayout.visibility = INITIAL_MULTIPLAYER_CAPABILITY_VISIBILITY
        binding.multiplayerCapabilitiesLinearLayout.visibility = INITIAL_MULTIPLAYER_CAPABILITY_VISIBILITY
    }

    companion object {
        const val RELEASE_REGION_SPINNER_REGION_DEFAULT = "North America"
        const val SIMILAR_GAME_TEXT_SIZE = 18F
        const val SIMILAR_GAME_LEFT_PADDING = 17
        const val SIMILAR_GAME_TOP_PADDING = 3
        const val SIMILAR_GAME_RIGHT_PADDING = 17
        const val SIMILAR_GAME_BOTTOM_PADDING = 3
        const val COUNTDOWN_TIMER_DELAY: Long = 500
        const val COUNTDOWN_TIMER_INTERVAL: Long = 500
        const val INITIAL_MULTIPLAYER_CAPABILITY_VISIBILITY = GONE

    }
}