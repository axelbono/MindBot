# 🧠MindBot
MindBot es una aplicación móvil de bienestar mental desarrollada en Android con Kotlin y Jetpack Compose. Combina inteligencia artificial conversacional con modelos psicológicos humanistas para ofrecer un espacio seguro, empático y personalizado donde los usuarios pueden expresar sus emociones y recibir apoyo emocional.



## 📹 Video demostración
https://vt.tiktok.com/ZSmTge6LJ/

## ⬇️ Descarga de la aplicación
https://axelbono.github.io/MentalBotAPP/

---

# 🚀 Características

- 💬 Chat con Inteligencia Artificial (Groq API)
- 🔐 Autenticación de usuarios (Login / Registro)
- ☁️ Sincronización en la nube con Firebase Firestore
- 📜 Historial de conversaciones
- 😊 Registro de estado de ánimo
- 🧘 Sección de bienestar emocional
- 🎨 Interfaz moderna con Jetpack Compose
- 🌙 Soporte para tema claro y oscuro
- 🧭 Navegación estructurada

---

# 🛠️ Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **UI Framework:** Jetpack Compose
- **Arquitectura:** MVVM + separación por capas
- **Base de datos:** Firebase Firestore
- **Autenticación:** Firebase Authentication
- **API IA:** Groq API
- **Asincronía:** Kotlin Coroutines + Flow
- **Build System:** Gradle Kotlin DSL

---

# 📋 Requisitos Previos

- Android Studio Hedgehog o superior
- JDK 11 o superior
- Cuenta de Firebase
- API Key válida de Groq
- Dispositivo Android API 24+ o emulador

---

# ⚙️ Instalación

## 1️⃣ Clonar repositorio

```bash
git clone https://github.com/axelbono/MindBot.git
```

## 2️⃣ Configurar Firebase

- Crear proyecto en Firebase Console
- Habilitar Authentication (Email/Password)
- Habilitar Cloud Firestore
- Descargar `google-services.json`
- Colocarlo dentro de la carpeta `app/`

## 3️⃣ Configurar API Key

Agregar tu API Key de Groq en el lugar correspondiente del proyecto.

## 4️⃣ Ejecutar

Abrir en Android Studio y compilar en un emulador o dispositivo físico.

---

# 🏗️ Arquitectura del Proyecto

El proyecto sigue una arquitectura inspirada en **Clean Architecture** utilizando el patrón **MVVM**.

## 📂 Estructura del Proyecto

```
MentalBot/
│
├── data/
│   ├── remote/
│   ├── repository/
│
├── domain/
│   ├── model/
│   ├── usecase/
│
├── ui/
│   ├── components/
│   ├── screens/
│   │   ├── chat/
│   │   ├── login/
│   │   ├── mood/
│   │   ├── wellness/
│   │
│   ├── navigation/
│   ├── viewmodel/
│
├── utils/
│
└── MainActivity.kt
```

---

## 🔎 Flujo Arquitectónico

```
UI (Compose)
      ↓
ViewModel
      ↓
UseCase
      ↓
Repository
      ↓
Firebase / Groq API
```

Esta separación permite:

- Código desacoplado
- Mejor mantenibilidad
- Escalabilidad
- Mayor facilidad para testing

---

# 📦 Modelos Principales

## 💬 Chat / Mensajes
Representan la conversación entre el usuario y la IA.

## 😊 Mood
Modelo que registra el estado emocional del usuario.

## 👤 Usuario
Incluye:
- id
- email
- fecha de creación

---

# 🧭 Navegación

El sistema de navegación centraliza el flujo entre pantallas:

- Login
- Registro
- Pantalla principal
- Chat
- Estado de ánimo
- Bienestar

---

# 📱 Capturas de Pantalla

Agrega aquí imágenes de la aplicación.

---

# 📦 Generación del APK

Para generar el APK firmado:

```
Build → Generate Signed Bundle / APK → APK
```

El archivo se encontrará en:

```
app/build/outputs/apk/release/
```

---

# 🔐 Seguridad

⚠️ Las API Keys no están incluidas en el repositorio.  
Debes configurarlas manualmente para evitar exposición de credenciales.

---

# 👨‍💻 Autor

**Axel Bono**

---

# 📄 Licencia

Proyecto desarrollado con fines académicos.
