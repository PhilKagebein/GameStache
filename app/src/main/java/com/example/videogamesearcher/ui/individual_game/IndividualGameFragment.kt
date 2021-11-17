package com.example.videogamesearcher.ui.individual_game

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.videogamesearcher.MainActivity
import com.example.videogamesearcher.R

class IndividualGameFragment : Fragment() {

    private val args: IndividualGameFragmentArgs by navArgs()

    companion object {
        fun newInstance() = IndividualGameFragment()
    }

    private lateinit var gameFragmentViewModel: IndividualGameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        gameFragmentViewModel = ViewModelProvider(this)[IndividualGameViewModel::class.java]
        val actionBar = (requireActivity() as MainActivity).supportActionBar?.setTitle(args.gameName)

        return inflater.inflate(R.layout.individual_game_fragment, container, false)
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gameId = args.gameId
    }

}