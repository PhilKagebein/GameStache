package com.example.videogamesearcher.ui.individual_game

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.videogamesearcher.MainActivity
import com.example.videogamesearcher.R
import com.example.videogamesearcher.SpinnerAdapter
import com.example.videogamesearcher.databinding.IndividualGameFragmentBinding
import com.example.videogamesearcher.models.individual_game.ReleaseDate

class IndividualGameFragment : Fragment() {

    private val args: IndividualGameFragmentArgs by navArgs()
    private lateinit var binding: IndividualGameFragmentBinding


    private lateinit var gameFragmentViewModel: IndividualGameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initIndividualGameViewModel()
        binding = IndividualGameFragmentBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).supportActionBar?.title = args.gameName

        binding.individualGameTitle.text = args.gameName

        binding.individualgameviewmodel = gameFragmentViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var summaryText: String? = ""
        var imageURL = ""
        var releaseDates: List<ReleaseDate?> = emptyList()

        gameFragmentViewModel.gameId.postValue(args.gameId)
        gameFragmentViewModel.getAccessToken()

        gameFragmentViewModel.createImageURLForGlide().observe(viewLifecycleOwner, { url ->
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

            val arrowButtonBackGroundResource = gameFragmentViewModel.determineArrowButtonStatus(binding.descriptionText.visibility)
            binding.descriptionBlockArrowButton.setBackgroundResource(arrowButtonBackGroundResource)

            binding.descriptionText.visibility = gameFragmentViewModel.changeCardViewVisibility(binding.descriptionText.visibility)

        }

        gameFragmentViewModel.summaryText.observe(viewLifecycleOwner, { liveSummaryText ->
            summaryText = liveSummaryText
        })

        binding.descriptionText.setOnClickListener {
            val gameSummaryDialog = summaryText?.let { summaryText -> GameSummaryDialog(summaryText) }
            gameSummaryDialog?.show(requireActivity().supportFragmentManager, "GameSummaryDialog")
        }

        //TODO: THINK ABOUT RACE CONDITION HERE
        gameFragmentViewModel.getIndividualGameData().observe(viewLifecycleOwner, { gameData ->
            gameData[0]?.release_dates?.let { it ->
                releaseDates = it
            } ?: run {
                ""
            }
        })

        gameFragmentViewModel.regionsList.observe(viewLifecycleOwner, { regionsList ->
            val releaseRegionsSpinner = initReleaseRegionSpinner(regionsList)

            setReleaseRegionSpinnerSelection(releaseRegionsSpinner, regionsList)
            setReleaseRegionSpinnerOnClick(releaseDates, releaseRegionsSpinner)
        })

        binding.releaseRegionsCardView.setOnClickListener {

            val arrowButtonBackGroundResource = gameFragmentViewModel.determineArrowButtonStatus(binding.releasesByRegionSpinner.visibility)
            binding.releasesByRegionArrowButton.setBackgroundResource(arrowButtonBackGroundResource)

            binding.releasesByRegionSpinner.visibility = gameFragmentViewModel.changeCardViewVisibility(binding.releasesByRegionSpinner.visibility)
            binding.releaseRegionInformationTextView.visibility = gameFragmentViewModel.changeCardViewVisibility(binding.releaseRegionInformationTextView.visibility)
        }

    }

    private fun setReleaseRegionSpinnerSelection(releaseRegionsSpinner: Spinner, regionsList: MutableList<String>) {
        if (regionsList.contains(RELEASE_REGION_SPINNER_REGION_DEFAULT)) {
            releaseRegionsSpinner.setSelection(regionsList.indexOf(RELEASE_REGION_SPINNER_REGION_DEFAULT))
        } else if (regionsList.size >= 2){
            releaseRegionsSpinner.setSelection(1)
        } else {
            releaseRegionsSpinner.setSelection(0)
        }
    }

    private fun initReleaseRegionSpinner(regionsList: MutableList<String>): Spinner {
        val releaseRegionsSpinner = binding.releasesByRegionSpinner
        val customAdapter = SpinnerAdapter(requireContext(), regionsList)
        releaseRegionsSpinner.adapter = customAdapter

        return releaseRegionsSpinner
    }

    private fun setReleaseRegionSpinnerOnClick(releaseDates: List<ReleaseDate?>, releaseRegionsSpinner: Spinner) {

        releaseRegionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowID: Long) {
                //TODO: TRY TO MAKE ALL THIS FUNCTIONALITY TIED TO LIVE DATA ANDMESS WITH THE RACE CONDITION ABOVE
                val releaseInformationText = gameFragmentViewModel.getReleaseInformationText(releaseDates, releaseRegionsSpinner.getItemAtPosition(itemPosition).toString())
                gameFragmentViewModel.releaseInformationText.postValue(releaseInformationText)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun initIndividualGameViewModel() {
        val factory =
            activity?.application?.let { IndividualGameViewModelFactory(resources, it)}
        gameFragmentViewModel = factory?.let { ViewModelProvider(this, it) }?.get(IndividualGameViewModel::class.java) as IndividualGameViewModel
    }

    companion object {
        const val RELEASE_REGION_SPINNER_REGION_DEFAULT = "North America"
    }
}