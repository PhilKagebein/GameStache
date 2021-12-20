package com.example.gamestache.ui.wishlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gamestache.databinding.FragmentWishlistBinding

class WishlistFragment : Fragment() {

    private lateinit var wishlistViewModel: WishlistViewModel
    private lateinit var binding: FragmentWishlistBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        wishlistViewModel = ViewModelProvider(this).get(WishlistViewModel::class.java)

        binding = FragmentWishlistBinding.inflate(inflater, container, false)

        return binding.root
    }

}