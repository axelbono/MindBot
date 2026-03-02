package com.bono.mentalbot.utils

import com.bono.mentalbot.BuildConfig

object Constants {

    const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"
    val GROQ_API_KEY = "Bearer ${BuildConfig.GROQ_API_KEY}"
    const val MODEL = "llama-3.1-8b-instant"

    fun getSystemPrompt(userName: String, mood: String): String {
        return """
Eres MindBot, un asistente de apoyo emocional empático y seguro.

El usuario se llama $userName. Dirígete a él siempre por su nombre de forma natural, cálida y cercana, sin sonar repetitivo.

El estado emocional actual del usuario es: $mood. Tenlo en cuenta, pero permite que cambie durante la conversación.

Tu estilo de respuesta debe ser:
- Calmado, validante y sin juicios.
- Breve y claro (máximo 5 oraciones).
- En español.
- Conversacional y humano, nunca robótico.
- Centrado en escuchar, razonar y pensar en lo mejor para el usuario antes de aconsejar.

Normas importantes:
- No diagnosticas ni haces afirmaciones clínicas.
- No reemplazas a un profesional de salud mental.
- Ofreces apoyo emocional, técnicas simples (respiración, grounding, mindfulness) o preguntas suaves cuando sea apropiado.
- No minimizas emociones ni usas frases vacías como "todo estará bien".
- Si detectas desesperanza intensa, autolesión o ideas suicidas, responde con empatía y recomienda buscar ayuda profesional o contactar un servicio de emergencia local de inmediato.
- En situaciones de riesgo, prioriza la seguridad por encima de la brevedad.

Tu objetivo es que $userName se sienta escuchado, comprendido y acompañado.
""".trimIndent()
    }

    fun getInitialMessage(
        userName: String,
        mood: String,
        hasWellbeingContext: Boolean = false
    ): String {
        val name = if (userName.isNotEmpty()) ", $userName" else ""

        return if (hasWellbeingContext) {
            when (mood.lowercase()) {
                "triste" -> "Hola$name 💙 Gracias por compartir cómo te sientes y por completar la evaluación. He podido conocerte un poco mejor y quiero que sepas que estoy aquí para ti. Noto que hoy estás triste... ¿quieres contarme qué está pasando?"
                "ansioso" -> "Hola$name 🌿 Gracias por tomarte el tiempo de completar la evaluación, eso dice mucho de ti. He notado aspectos importantes de tu situación actual y quiero ayudarte. Sé que hoy te sientes ansioso... respira profundo, estoy aquí contigo. ¿Qué te tiene preocupado?"
                "enojado" -> "Hola$name 🔴 Gracias por compartir cómo estás a través de la evaluación. Entiendo que hoy estás enojado y es completamente válido sentirte así. Con lo que me has contado, puedo entender mejor tu situación. ¿Quieres hablar sobre lo que pasó?"
                "cansado" -> "Hola$name 🌙 Gracias por completar la evaluación, sé que cuando uno está cansado hacer cualquier cosa requiere esfuerzo. He visto tu situación actual y quiero apoyarte. ¿Ha sido un período muy pesado para ti?"
                "feliz" -> "Hola$name 🌟 ¡Qué bueno verte por aquí! Gracias por compartir tu evaluación, me ayuda a conocerte mejor. Me alegra mucho que hoy te sientas feliz. ¿Qué es lo que te tiene con esa energía tan bonita hoy?"
                "tranquilo" -> "Hola$name 🍃 Gracias por completar la evaluación, es un gesto de autocuidado hermoso. He podido conocer un poco más sobre ti y tu momento actual. Me alegra que hoy estés tranquilo. ¿Cómo te gustaría aprovechar esta calma?"
                else -> "Hola$name 👋 Gracias por completar la evaluación emocional, eso me permite conocerte mejor y darte un apoyo más personalizado. ¿Cómo te sientes en este momento?"
            }
        } else {
            when (mood.lowercase()) {
                "triste" -> "Hola$name 💙 He notado que hoy te sientes triste. ¿Quieres contarme qué ha pasado? Estoy aquí para escucharte."
                "ansioso" -> "Hola$name 🌿 Veo que hoy te sientes ansioso. Respira profundo... estoy aquí contigo. ¿Qué te tiene preocupado?"
                "enojado" -> "Hola$name 🔴 Entiendo que hoy estás enojado. Es completamente válido sentirte así. ¿Qué fue lo que pasó?"
                "cansado" -> "Hola$name 🌙 Parece que hoy estás muy cansado. El descanso es importante. ¿Ha sido un día muy pesado?"
                "feliz" -> "Hola$name 🌟 ¡Me alegra mucho que hoy te sientas feliz! ¿Qué te tiene con esa energía tan bonita?"
                "tranquilo" -> "Hola$name 🍃 Qué bien que hoy te sientes tranquilo. ¿Cómo te gustaría aprovechar esta calma hoy?"
                else -> "Hola$name 👋 ¿Cómo estás hoy? Cuéntame, estoy aquí para escucharte."
            }
        }
    }
}