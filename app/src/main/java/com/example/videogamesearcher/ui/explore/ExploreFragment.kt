package com.example.videogamesearcher.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.videogamesearcher.R
import com.example.videogamesearcher.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {

    private lateinit var exploreViewModel: ExploreViewModel
    private var _binding: FragmentExploreBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initExploreViewModel()
        //exploreViewModel = ViewModelProvider(this).get(ExploreViewModel::class.java)

        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var spnPlatform = binding.spnPlatform
        var spnGenre = binding.spnGenre
        var spnMultiplayer = binding.spnMultiplayer

        spnPlatform = initSpinners(spnPlatform, exploreViewModel.getPlatformStrArray())
        spnGenre = initSpinners(spnGenre, exploreViewModel.getGenreStrArray())
        spnMultiplayer = initSpinners(spnMultiplayer, exploreViewModel.getMultiplayerStrArray())

        spnPlatform.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
            }

            override fun onNothingSelected(adapterview: AdapterView<*>?) {

            }
        }
        spnGenre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
            }

            override fun onNothingSelected(adapterview: AdapterView<*>?) {

            }
        }
        spnMultiplayer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, itemPosition: Int, rowId: Long) {
            }

            override fun onNothingSelected(adapterview: AdapterView<*>?) {

            }
        }

        binding.btnExploreSearch.setOnClickListener {

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initSpinners(spinner: Spinner, strArray: Array<String>): Spinner {
        spinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_items, strArray)

        return spinner
    }


    private fun initExploreViewModel(){
        val factory = activity?.let {ExploreViewModelFactory(it.application, resources)}
        exploreViewModel = factory?.let {ViewModelProvider(this, it) }?.get(ExploreViewModel::class.java) as ExploreViewModel
    }
}