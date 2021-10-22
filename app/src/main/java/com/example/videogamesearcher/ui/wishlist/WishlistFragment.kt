package com.example.videogamesearcher.ui.wishlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.videogamesearcher.databinding.FragmentWishlistBinding

class WishlistFragment : Fragment() {

    private lateinit var wishlistViewModel: WishlistViewModel
    private lateinit var binding: FragmentWishlistBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        wishlistViewModel = ViewModelProvider(this).get(WishlistViewModel::class.java)

        binding = FragmentWishlistBinding.inflate(inflater, container, false)

        val textView: TextView = binding.textNotifications
        wishlistViewModel.text.observe(viewLifecycleOwner, { textView.text = it })
        return binding.root
    }

}