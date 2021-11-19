package com.example.videogamesearcher.ui.individual_game

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.videogamesearcher.R
import kotlinx.android.synthetic.main.individual_game_art_dialog.view.*

class ArtDialog(private val url: String): DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView: View = inflater.inflate(R.layout.individual_game_art_dialog, container, false)

        Glide.with(this)
            .load(url)
            .placeholder(R.color.transparent)
            .error(R.color.transparent)
            .into(rootView.artDialogImageView)

        rootView.cancelButton.setOnClickListener {
            dismiss()
        }

        rootView.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                ScaleGestureDetector(requireContext(), PinchZoomListener(rootView.artDialogImageView)).onTouchEvent(event)
                return true
            }
        })
        return rootView
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if(dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

}