package com.example.videogamesearcher.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videogamesearcher.R
import com.example.videogamesearcher.databinding.FragmentExploreBinding
import com.example.videogamesearcher.models.TwitchAuthorization
import com.example.videogamesearcher.repository.Repository
import okhttp3.RequestBody


class ExploreFragment : Fragment() {

    private lateinit var exploreViewModel: ExploreViewModel
    private var _binding: FragmentExploreBinding? = null
    private val repository = Repository()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initExploreViewModel()

        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        binding.exploreviewmodel = exploreViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var spnPlatform = binding.spnPlatform
        var spnGenre = binding.spnGenre
        var spnMultiplayer = binding.spnMultiplayer
        var authorization: TwitchAuthorization?
        lateinit var searchText : RequestBody

        val exploreAdapter = GamesListSearchResultsAdapter(resources)
        binding.rvExplore.apply{
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = exploreAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
        exploreViewModel.getAccessToken()

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
            exploreViewModel.twitchAuthorization.value?.access_token?.let {twitchAccessToken ->
                    exploreViewModel.searchGames(twitchAccessToken, searchText)
                }
        }

        exploreViewModel.authorizationResponse.observe(viewLifecycleOwner, { response ->
            if(response.isSuccessful){
                authorization = response.body()
                exploreViewModel.twitchAuthorization.postValue(authorization)
                println(authorization?.access_token)
            }//add an else statement later
        })

        exploreViewModel.searchText.observe(viewLifecycleOwner, { text ->
            searchText = text
        })

        //try putting this in the view model
        exploreViewModel.gamesListResponse.observe(viewLifecycleOwner, { response ->
            if(response.isSuccessful){
                exploreViewModel.gamesListResults.postValue(response.body())
            }
        })

        exploreViewModel.gamesListResults.observe(viewLifecycleOwner, {
            exploreAdapter.submitList(it)
        })
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
        val factory = activity?.let {ExploreViewModelFactory(repository, it.application, resources)}
        exploreViewModel = factory?.let {ViewModelProvider(this, it) }?.get(ExploreViewModel::class.java) as ExploreViewModel
    }

}