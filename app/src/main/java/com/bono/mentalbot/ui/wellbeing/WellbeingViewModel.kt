package com.bono.mentalbot.ui.wellbeing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Estado de la evaluación de bienestar emocional que el usuario completa.
 *
 * Los valores están diseñados para ofrecer una foto rápida del estado emocional
 * en distintas dimensiones (Maslow, Ortega, López de Llergo).
 */
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

/**
 * ViewModel que almacena y actualiza los valores de la evaluación emocional.
 *
 * Permite a la UI reaccionar automáticamente a los cambios mediante StateFlow.
 */
class WellbeingViewModel : ViewModel() {

    private val _data = MutableStateFlow(WellbeingData())
    val data: StateFlow<WellbeingData> = _data

    /**
     * Actualiza el valor de "necesidades básicas" (Maslow).
     */
    fun updateNecesidadesBasicas(value: Float) {
        _data.value = _data.value.copy(necesidadesBasicas = value)
    }

    /**
     * Actualiza el valor de "seguridad vital" (Maslow).
     */
    fun updateSeguridadVital(value: Float) {
        _data.value = _data.value.copy(seguridadVital = value)
    }

    /**
     * Actualiza el valor de "relaciones sociales" (Maslow).
     */
    fun updateRelacionesSociales(value: Float) {
        _data.value = _data.value.copy(relacionesSociales = value)
    }

    /**
     * Actualiza el valor de "autoestima" (Maslow).
     */
    fun updateAutoestima(value: Float) {
        _data.value = _data.value.copy(autoestima = value)
    }

    /**
     * Actualiza el valor de "propósito de vida" (Maslow).
     */
    fun updatePropositoVida(value: Float) {
        _data.value = _data.value.copy(propositoVida = value)
    }

    /**
     * Actualiza el valor de "satisfacción con las circunstancias" (Ortega).
     */
    fun updateSatisfaccionCircunstancias(value: Float) {
        _data.value = _data.value.copy(satisfaccionCircunstancias = value)
    }

    /**
     * Actualiza la percepción de "control sobre la vida" (Ortega).
     */
    fun updateControlVida(value: Float) {
        _data.value = _data.value.copy(controlVida = value)
    }

    /**
     * Actualiza el texto descriptivo de la circunstancia actual (Ortega).
     */
    fun updateTextoCircunstancia(value: String) {
        _data.value = _data.value.copy(textoCircunstancia = value)
    }

    /**
     * Actualiza la vivencia de los valores personales (López de Llergo).
     */
    fun updateVivenciaValores(value: Float) {
        _data.value = _data.value.copy(vivenciaValores = value)
    }

    /**
     * Actualiza el texto que describe qué valores son relevantes en este momento.
     */
    fun updateTextoValores(value: String) {
        _data.value = _data.value.copy(textoValores = value)
    }

    /**
     * Actualiza el texto libre que el usuario quiere compartir.
     */
    fun updateTextoLibre(value: String) {
        _data.value = _data.value.copy(textoLibre = value)
    }

    /**
     * Construye un bloque de texto (contexto) que resume la evaluación emocional.
     *
     * Este contexto se puede enviar al asistente para que tenga más información
     * del usuario antes de generar una respuesta.
     */
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