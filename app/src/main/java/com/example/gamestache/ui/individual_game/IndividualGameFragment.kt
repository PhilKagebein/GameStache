package com.example.gamestache.ui.individual_game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.gamestache.MainActivity
import com.example.gamestache.R
import com.example.gamestache.SpinnerAdapter
import com.example.gamestache.databinding.IndividualGameFragmentBinding
import com.example.gamestache.models.individual_game.ReleaseDate
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.NullPointerException

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

        var summaryText: String? = ""
        var imageURL = ""
        val releaseRegionSpinner = binding.releasesByRegionSpinner

        individualGameViewModel.gameId.postValue(args.gameId)
        individualGameViewModel.getAccessToken()

        individualGameViewModel.createImageURLForGlide().observe(viewLifecycleOwner, { url ->
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
            individualGameViewModel.getReleaseDatesList().observe(viewLifecycleOwner, { releaseDates ->
                val spinner = initReleaseRegionSpinner(regionsList, releaseRegionSpinner)

                setReleaseRegionSpinnerSelection(spinner, regionsList)
                setOnClickForReleaseRegionSpinner(releaseDates, releaseRegionSpinner)
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

    private fun initReleaseRegionSpinner(regionsList: MutableList<String>, spinner: Spinner): Spinner {
        val customAdapter = SpinnerAdapter(requireContext(), regionsList)
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

    companion object {
        const val RELEASE_REGION_SPINNER_REGION_DEFAULT = "North America"
    }
}