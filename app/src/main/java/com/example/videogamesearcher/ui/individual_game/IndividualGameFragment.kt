package com.example.videogamesearcher.ui.individual_game

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.videogamesearcher.MainActivity
import com.example.videogamesearcher.R
import com.example.videogamesearcher.databinding.IndividualGameFragmentBinding

class IndividualGameFragment : Fragment() {

    private val args: IndividualGameFragmentArgs by navArgs()
    private var _binding: IndividualGameFragmentBinding? = null
    private  var dialogArtView: ImageView? = null

    private val binding get() = _binding!!

    companion object {
        fun newInstance() = IndividualGameFragment()
    }

    private lateinit var gameFragmentViewModel: IndividualGameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        gameFragmentViewModel = ViewModelProvider(this)[IndividualGameViewModel::class.java]
        _binding = IndividualGameFragmentBinding.inflate(inflater, container, false)
        val actionBar = (requireActivity() as MainActivity).supportActionBar?.setTitle(args.gameName)

        return binding.root
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            dialogArtView = getView()?.findViewById(R.id.artDialogImageView)
            artDialog.show(requireActivity().supportFragmentManager, "ArtDialog")
        }

    }
}