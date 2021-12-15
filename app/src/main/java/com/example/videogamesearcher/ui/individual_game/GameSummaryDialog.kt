package com.example.videogamesearcher.ui.individual_game

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.videogamesearcher.R
import kotlinx.android.synthetic.main.game_summary_dialog.view.*

class GameSummaryDialog(private val summaryText: String): DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView: View = inflater.inflate(R.layout.game_summary_dialog, container, false)

        val summaryTextView = rootView.findViewById<TextView>(R.id.gameSummaryDialogTextView)
        summaryTextView.movementMethod = ScrollingMovementMethod()
        summaryTextView.text = summaryText

        rootView.cancelButtonGameSummary.setOnClickListener {
            dismiss()
        }

        return rootView
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))
        if(dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }
}