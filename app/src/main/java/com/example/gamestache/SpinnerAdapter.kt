package com.example.gamestache

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SpinnerAdapter(context: Context, spinnerList: MutableList<String?>):
    ArrayAdapter<String>(context, 0, spinnerList) {

        val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.spinner_items, null, true)
        val item = getItem(position)
        val textView = rowView.findViewById<TextView>(R.id.spinner_items)
        textView.text = item.toString()
        return rowView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val dialogView = inflater.inflate(R.layout.spinner_dialog, parent, false)
        val item = getItem(position)
        val textView = dialogView?.findViewById<TextView>(R.id.spinnerField)
        if (position == 0) {
            textView?.visibility = GONE
        }
            textView?.text = item.toString()
            return dialogView
        }


}