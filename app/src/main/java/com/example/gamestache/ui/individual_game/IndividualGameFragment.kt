package com.example.gamestache.ui.individual_game

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
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
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem
import com.example.gamestache.models.individual_game.MultiplayerModesItem
import com.example.gamestache.models.individual_game.ReleaseDate
import com.example.gamestache.models.individual_game.SimilarGame
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
        individualGameViewModel.getAccessToken()

        individualGameViewModel.progressBarIsVisible.observe(viewLifecycleOwner, { progressBarStatus ->

            if (progressBarStatus) {
                loadingDialog.show()
            } else {
                object : CountDownTimer(500, 500) {
                    override fun onFinish() {
                        loadingDialog.dismiss()
                    }

                    override fun onTick(p0: Long) {
                    }

                }.start()
            }

        })

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

        binding.cardViewSummary.setOnClickListener {

            val arrowButtonBackGroundResource = individualGameViewModel.determineArrowButtonStatus(binding.descriptionText.visibility)
            binding.descriptionBlockArrowButton.setBackgroundResource(arrowButtonBackGroundResource)

            binding.descriptionText.visibility = individualGameViewModel.changeCardViewVisibility(binding.descriptionText.visibility)

        }

        individualGameViewModel.summaryText.observe(viewLifecycleOwner, { liveSummaryText ->
            summaryText = liveSummaryText
        })

        binding.descriptionText.setOnClickListener {
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
                        binding.multiplayerCapabilitiesSectionTitleTV.visibility = GONE
                        multiplayerOnPlatformSpinner.visibility = GONE
                        binding.coopCapabilitiesTV.visibility = GONE
                        binding.offlineCapabilities.visibility = GONE
                        binding.onlineCapabilities.visibility = GONE
                        binding.onlineCapablitiesTitleTV.visibility = GONE
                        binding.offlineCapabilitiesTitleTV.visibility = GONE
                        binding.coopCapabilitiesTitleTV.visibility = GONE
                    }
                    val spinner = initSpinner(platformListForSpinner, multiplayerOnPlatformSpinner)

                    setOnClickForMultiplayerPlatformSpinner(multiplayerModesList, spinner, platformListFromDb)
                })
            })
        })

        binding.releaseRegionsCardView.setOnClickListener {

            val arrowButtonBackGroundResource = individualGameViewModel.determineArrowButtonStatus(binding.releasesByRegionSpinner.visibility)
            binding.releasesByRegionArrowButton.setBackgroundResource(arrowButtonBackGroundResource)

            binding.releasesByRegionSpinner.visibility = individualGameViewModel.changeCardViewVisibility(binding.releasesByRegionSpinner.visibility)
            binding.releaseRegionInformationTextView.visibility = individualGameViewModel.changeCardViewVisibility(binding.releaseRegionInformationTextView.visibility)
        }

        binding.cardViewGenresAndPerspectives.setOnClickListener {

            val arrowButtonBackGroundResource = individualGameViewModel.determineArrowButtonStatus(binding.individualGamePlayerPerspectivesTV.visibility)
            binding.playerPerspectivesAndGenresArrowButton.setBackgroundResource(arrowButtonBackGroundResource)

            binding.individualGameGenresTV.visibility = individualGameViewModel.changeCardViewVisibility(binding.individualGameGenresTV.visibility)
            binding.individualGamePlayerPerspectivesTV.visibility = individualGameViewModel.changeCardViewVisibility(binding.individualGamePlayerPerspectivesTV.visibility)

        }

        individualGameViewModel.multiplayerModesList.observe(viewLifecycleOwner, { multiplayerModes ->
            binding.gameModesCardView.setOnClickListener {

                val arrowButtonBackGroundResource = individualGameViewModel.determineArrowButtonStatus(binding.gameModesTV.visibility)
                binding.gameModesArrowButton.setBackgroundResource(arrowButtonBackGroundResource)

                binding.gameModesTV.visibility = individualGameViewModel.changeCardViewVisibility(binding.gameModesTV.visibility)
                binding.multiplayerCapabilitiesSectionTitleTV.visibility = individualGameViewModel.getMultiplayerTitleAndSpinnerVisibility( binding.multiplayerCapabilitiesSectionTitleTV.visibility, multiplayerModes)
                multiplayerOnPlatformSpinner.visibility = individualGameViewModel.getMultiplayerTitleAndSpinnerVisibility(multiplayerOnPlatformSpinner.visibility, multiplayerModes)

                //TODO: IF ALL OF THESE WILL ALWAYS SHARE THE SAME VISIBILITY, DO I WRAP THESE AROUND A LINEAR LAYOUT OR SOMETHING AND JUST CHANGE THE VISIBILITY ON THAT?
                binding.coopCapabilitiesTV.visibility = individualGameViewModel.getMultiplayerCapabilitiesInfoVisibility(multiplayerOnPlatformSpinner.selectedItemPosition, binding.coopCapabilitiesTV.visibility)
                binding.offlineCapabilities.visibility = individualGameViewModel.getMultiplayerCapabilitiesInfoVisibility(multiplayerOnPlatformSpinner.selectedItemPosition, binding.offlineCapabilities.visibility)
                binding.onlineCapabilities.visibility = individualGameViewModel.getMultiplayerCapabilitiesInfoVisibility(multiplayerOnPlatformSpinner.selectedItemPosition, binding.onlineCapabilities.visibility)
                binding.onlineCapablitiesTitleTV.visibility = individualGameViewModel.getMultiplayerCapabilitiesInfoVisibility(multiplayerOnPlatformSpinner.selectedItemPosition, binding.onlineCapablitiesTitleTV.visibility)
                binding.offlineCapabilitiesTitleTV.visibility = individualGameViewModel.getMultiplayerCapabilitiesInfoVisibility(multiplayerOnPlatformSpinner.selectedItemPosition, binding.offlineCapabilitiesTitleTV.visibility)
                binding.coopCapabilitiesTitleTV.visibility = individualGameViewModel.getMultiplayerCapabilitiesInfoVisibility(multiplayerOnPlatformSpinner.selectedItemPosition, binding.coopCapabilitiesTitleTV.visibility)

            }
        })

        binding.similarGamesCardView.setOnClickListener {
            val arrowButtonBackGroundResource = individualGameViewModel.determineArrowButtonStatus(similarGamesTextViews[0].visibility)
            binding.similarGamesArrowButton.setBackgroundResource(arrowButtonBackGroundResource)

            for (textView in similarGamesTextViews.indices) {
                similarGamesTextViews[textView].visibility = individualGameViewModel.changeCardViewVisibility(similarGamesTextViews[textView].visibility)
            }
        }

        individualGameViewModel.similarGamesList.observe(viewLifecycleOwner, { similarGamesList ->
            populateSimilarGamesTextViews(similarGamesList, count, similarGamesTextViews)
            count++
            formatSimilarGamesTextViews(similarGamesTextViews)
            setSimilarGameOnClickListener(similarGamesList, similarGamesTextViews)
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
        for (textView in similarGamesTextViews) {
            textView.apply {
                textSize = SIMILAR_GAME_TEXT_SIZE
                setPadding(SIMILAR_GAME_LEFT_PADDING, SIMILAR_GAME_TOP_PADDING, SIMILAR_GAME_RIGHT_PADDING, SIMILAR_GAME_BOTTOM_PADDING)
                setTextColor(resources.getColor(R.color.white, context.theme))
                textAlignment = left
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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
        multiplayerOnPlatformSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {

                getMultiplayerModeItemVisibility(itemPosition).apply {
                    binding.coopCapabilitiesTV.visibility = this
                    binding.coopCapabilitiesTitleTV.visibility = this
                    binding.offlineCapabilities.visibility = this
                    binding.offlineCapabilitiesTitleTV.visibility = this
                    binding.onlineCapabilities.visibility = this
                    binding.onlineCapablitiesTitleTV.visibility = this
                }

                val coopCapabilitiesText: String = individualGameViewModel.getCoopCapabilitiesText(multiplayerModes, spinner.getItemAtPosition(itemPosition).toString(), platformListFromDb)
                individualGameViewModel.coopCapabilitiesText.postValue(coopCapabilitiesText)

                val offlineCapabilitiesText: String = individualGameViewModel.getOfflineCapabilitiesText(multiplayerModes, spinner.getItemAtPosition(itemPosition).toString(), platformListFromDb)
                individualGameViewModel.offlineCapabilitiesText.postValue(offlineCapabilitiesText)

                val onlineCapabilitiesText: String = individualGameViewModel.getOnlineCapabilitiesText(multiplayerModes, spinner.getItemAtPosition(itemPosition).toString(), platformListFromDb)
                individualGameViewModel.onlineCapabilitiesText.postValue(onlineCapabilitiesText)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    companion object {
        const val RELEASE_REGION_SPINNER_REGION_DEFAULT = "North America"
        const val SIMILAR_GAME_TEXT_SIZE = 18F
        const val SIMILAR_GAME_LEFT_PADDING = 17
        const val SIMILAR_GAME_TOP_PADDING = 3
        const val SIMILAR_GAME_RIGHT_PADDING = 17
        const val SIMILAR_GAME_BOTTOM_PADDING = 3

    }
}