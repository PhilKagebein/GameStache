package com.example.videogamesearcher.ui.individual_game

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.videogamesearcher.MainActivity
import com.example.videogamesearcher.R
import com.example.videogamesearcher.databinding.IndividualGameFragmentBinding

class IndividualGameFragment : Fragment() {

    private val args: IndividualGameFragmentArgs by navArgs()
    private lateinit var binding: IndividualGameFragmentBinding


    private lateinit var gameFragmentViewModel: IndividualGameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        gameFragmentViewModel = ViewModelProvider(this)[IndividualGameViewModel::class.java]
        binding = IndividualGameFragmentBinding.inflate(inflater, container, false)
        val actionBar = (requireActivity() as MainActivity).supportActionBar?.setTitle(args.gameName)

        binding.individualGameTitle.text = args.gameName

        binding.individualgameviewmodel = gameFragmentViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var summaryText: String? = ""
        var imageURL = ""
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

        //TODO: TALK TO KEVIN ABOUT IF THERE'S A WAY TO NOT REPEAT THE SAME BLOCK OF CODE.
        // WITHOUT THE ONCLICK FOR THE ARROWBUTTON, NOTHING HAPPENS. SEEMS LIKE IT'S "ABOVE/IN FRONT OF" THE CARD VIEW
        binding.cardViewSummary.setOnClickListener {

            val arrowButtonBackGroundResource = gameFragmentViewModel.determineArrowButtonStatus(binding.descriptionText.visibility)
            binding.descriptionBlockArrowButton.setBackgroundResource(arrowButtonBackGroundResource)

            binding.descriptionText.visibility = gameFragmentViewModel.setDescriptionTextVisibility(binding.descriptionText.visibility)

        }

        binding.descriptionBlockArrowButton.setOnClickListener {

            val arrowButtonBackGroundResource = gameFragmentViewModel.determineArrowButtonStatus(binding.descriptionText.visibility)
            binding.descriptionBlockArrowButton.setBackgroundResource(arrowButtonBackGroundResource)

            binding.descriptionText.visibility = gameFragmentViewModel.setDescriptionTextVisibility(binding.descriptionText.visibility)

        }

        gameFragmentViewModel.summaryText.observe(viewLifecycleOwner, { liveSummaryText ->
            summaryText = liveSummaryText
        })

        binding.descriptionText.setOnClickListener {
            val gameSummaryDialog = summaryText?.let { summaryText -> GameSummaryDialog(summaryText) }
            gameSummaryDialog?.show(requireActivity().supportFragmentManager, "GameSummaryDialog")
        }
    }
}