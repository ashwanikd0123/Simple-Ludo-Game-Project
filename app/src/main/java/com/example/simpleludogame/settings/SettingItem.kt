package com.example.simpleludogame.settings

sealed class SettingItem {
    data class Header(val title: String) : SettingItem()
    data class Option(
        val title: String,
        val options: List<String>,
        val selectedIndex: Int,
        val onOptionSelected: (Int) -> Unit
    ) : SettingItem()
}
