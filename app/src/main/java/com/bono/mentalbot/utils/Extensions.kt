package com.bono.mentalbot.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Convierte un timestamp (milisegundos desde epoch) a una cadena con formato legible.
 *
 * @receiver Timestamp en milisegundos.
 * @return Fecha y hora formateadas en el formato "dd/MM/yyyy HH:mm".
 */
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Capitaliza la primera letra de la cadena según la configuración regional actual.
 *
 * @receiver Cadena de texto a capitalizar.
 * @return La misma cadena con la primera letra en mayúscula (si es minúscula).
 */
fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}