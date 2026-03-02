package com.bono.mentalbot.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Convierte un timestamp Long a fecha legible
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

// Capitaliza la primera letra de un String
fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}