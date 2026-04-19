package com.example.simpleludogame.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.example.simpleludogame.R

class SettingsAdapter(
    private val context: Context,
    private val items: List<SettingItem>
) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getViewTypeCount(): Int = 2
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SettingItem.Header -> 0
            is SettingItem.Option -> 1
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = items[position]
        return when (item) {
            is SettingItem.Header -> {
                val view = convertView ?: inflater.inflate(R.layout.setting_item_header, parent, false)
                view.findViewById<TextView>(R.id.header_title).text = item.title
                view
            }
            is SettingItem.Option -> {
                val view = convertView ?: inflater.inflate(R.layout.setting_item_option, parent, false)
                view.findViewById<TextView>(R.id.setting_title).text = item.title
                val radioGroup = view.findViewById<RadioGroup>(R.id.setting_radio_group)
                
                radioGroup.removeAllViews()
                item.options.forEachIndexed { index, optionText ->
                    val radioButton = RadioButton(context).apply {
                        text = optionText
                        id = View.generateViewId()
                    }
                    radioGroup.addView(radioButton)
                    if (index == item.selectedIndex) {
                        radioGroup.check(radioButton.id)
                    }
                }

                radioGroup.setOnCheckedChangeListener { group, checkedId ->
                    val checkedIndex = group.indexOfChild(group.findViewById(checkedId))
                    item.onOptionSelected(checkedIndex)
                }
                view
            }
        }
    }
}
