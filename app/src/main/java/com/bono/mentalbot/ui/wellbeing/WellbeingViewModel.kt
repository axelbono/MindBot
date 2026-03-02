package com.bono.mentalbot.ui.wellbeing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class WellbeingData(
    // Maslow
    val necesidadesBasicas: Float = 3f,
    val seguridadVital: Float = 3f,
    val relacionesSociales: Float = 3f,
    val autoestima: Float = 3f,
    val propositoVida: Float = 3f,
    // Ortega
    val satisfaccionCircunstancias: Float = 3f,
    val controlVida: Float = 3f,
    val textoCircunstancia: String = "",
    // Lopez de Llergo
    val vivenciaValores: Float = 3f,
    val textoValores: String = "",
    val textoLibre: String = ""
)

class WellbeingViewModel : ViewModel() {

    private val _data = MutableStateFlow(WellbeingData())
    val data: StateFlow<WellbeingData> = _data

    fun updateNecesidadesBasicas(value: Float) {
        _data.value = _data.value.copy(necesidadesBasicas = value)
    }

    fun updateSeguridadVital(value: Float) {
        _data.value = _data.value.copy(seguridadVital = value)
    }

    fun updateRelacionesSociales(value: Float) {
        _data.value = _data.value.copy(relacionesSociales = value)
    }

    fun updateAutoestima(value: Float) {
        _data.value = _data.value.copy(autoestima = value)
    }

    fun updatePropositoVida(value: Float) {
        _data.value = _data.value.copy(propositoVida = value)
    }

    fun updateSatisfaccionCircunstancias(value: Float) {
        _data.value = _data.value.copy(satisfaccionCircunstancias = value)
    }

    fun updateControlVida(value: Float) {
        _data.value = _data.value.copy(controlVida = value)
    }

    fun updateTextoCircunstancia(value: String) {
        _data.value = _data.value.copy(textoCircunstancia = value)
    }

    fun updateVivenciaValores(value: Float) {
        _data.value = _data.value.copy(vivenciaValores = value)
    }

    fun updateTextoValores(value: String) {
        _data.value = _data.value.copy(textoValores = value)
    }

    fun updateTextoLibre(value: String) {
        _data.value = _data.value.copy(textoLibre = value)
    }

    fun buildWellbeingContext(): String {
        val d = _data.value
        return """
            EVALUACIÓN EMOCIONAL DEL USUARIO:
            
            [MASLOW - Necesidades]
            - Necesidades básicas cubiertas: ${d.necesidadesBasicas}/5
            - Sensación de seguridad vital: ${d.seguridadVital}/5
            - Calidad de relaciones sociales: ${d.relacionesSociales}/5
            - Nivel de autoestima: ${d.autoestima}/5
            - Sentido de propósito: ${d.propositoVida}/5
            
            [ORTEGA - Circunstancias vitales]
            - Satisfacción con sus circunstancias actuales: ${d.satisfaccionCircunstancias}/5
            - Sensación de control sobre su vida: ${d.controlVida}/5
            - El usuario describe su situación así: "${d.textoCircunstancia}"
            
            [LÓPEZ DE LLERGO - Valores]
            - Vivencia de sus valores personales: ${d.vivenciaValores}/5
            - Lo que más valora en este momento: "${d.textoValores}"
            
            [REFLEXIÓN LIBRE]
            - El usuario quiere que sepas: "${d.textoLibre}"
            
            Usa toda esta información para personalizar tu respuesta de forma empática y profunda.
        """.trimIndent()
    }
}