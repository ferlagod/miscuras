# Mis Curas — Guía de Apósitos para Enfermeras

![Banner de Mis Curas](https://i.imgur.com/IEFXtMX.png)

**Mis Curas** es una aplicación móvil moderna y ligera construida con Android, Kotlin y Jetpack Compose, diseñada exclusivamente como una herramienta de apoyo a la toma de decisiones clínicas para profesionales de enfermería en el tratamiento de heridas crónicas y agudas.

Las recomendaciones sugeridas por la aplicación se fundamentan estrictamente en las directrices y documentos de consenso del **GNEAUPP** (Grupo Nacional para el Estudio y Asesoramiento en Úlceras por Presión y Heridas Crónicas) y en el catálogo habitual de productos del **Servicio Andaluz de Salud (SAS)**.

---

## 🌟 Características Principales

- 🩺 **Selector Clínico Inteligente:** Evalúa el estado del lecho de la herida, el nivel de exudado y la presencia de signos de infección para obtener recomendaciones instantáneas.
- 🔗 **Soporte de Múltiples Familias:** Capacidad única para recomendar múltiples familias de apósitos compatibles simultáneamente (ej. *Hidrogeles* combinados con *Desbridantes Enzimáticos*), unificando los resultados en una sola lista visual.
- 📦 **Catálogo de Productos SAS:** Información exhaustiva de apósitos reales con su **código nacional (CN)**, fabricante, medidas comerciales exactas y mecanismo de acción detallado.
- ⚠️ **Control de Interacciones:** Alerta al profesional sobre posibles incompatibilidades, interacciones y precauciones entre apósitos y otros productos antes de la aplicación.
- 🎨 **Interfaz de Alta Calidad:** Diseño fluido, premium y minimalista adaptado por defecto a **Tema Oscuro** y con soporte completo para Tema Claro o del Sistema.
- 🌐 **Soporte Multi-idioma:** Traducido de forma instantánea a **Español**, **Inglés** y **Portugués**.
- 🔌 **Funcionamiento 100% Offline:** Toda la base de datos de productos y reglas clínicas se aloja en una base de datos local Room SQLite, permitiendo su uso en entornos hospitalarios o de atención domiciliaria sin necesidad de conexión a Internet.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Kotlin
- **Diseño UI:** Jetpack Compose (Material Design 3)
- **Base de Datos Local:** Room Database (SQLite) con carga automatizada de catálogos mediante parseo de ficheros raw CSV.
- **Persistencia de Preferencias:** SharedPreferences para recordar la configuración de tema e idioma.
- **Carga de Imágenes:** Coil (diseñado específicamente para optimizar el renderizado y memoria de las miniaturas de apósitos locales).

---

## ⚖️ Licencia y Propiedad de los Datos

Este proyecto se divide en dos componentes diferenciados bajo las siguientes condiciones legales:

1. **Código Fuente (Software):** Este proyecto es software libre y se distribuye bajo los términos de la licencia GNU GPLv3. Consultar el archivo `LICENSE` para más detalles. Puedes estudiar el código, auditarlo y contribuir a su desarrollo en [ForjaLibre](https://forjalibre.eu/ferlagod/miscuras).
2. **Datos Clínicos y de Catálogo (Contenido):** **Los datos de los productos y el catálogo no son libres**. El archivo de productos recopila información comercial, códigos nacionales y marcas registradas protegidos por sus respectivos fabricantes y distribuidores de salud. Queda prohibida la explotación comercial o redistribución masiva de este catálogo de datos sin autorización explícita.

---

## ⚠️ Descargo de Responsabilidad (Disclaimer)

Esta aplicación no sustituye en ningún caso el juicio clínico, la valoración directa de la herida por parte del personal de enfermería ni la evaluación integral del paciente. La selección final del apósito o tratamiento es responsabilidad exclusiva del profesional sanitario a cargo, quien debe considerar las características individuales y alergias de cada caso.

El desarrollador no asume ninguna responsabilidad por decisiones clínicas tomadas con base en la información proporcionada en esta aplicación, ni por la evolución, complicaciones o resultados derivados de los tratamientos aquí sugeridos.

---

## 💙 Apoyar y Contactar al Desarrollador

Si esta herramienta te resulta útil en tu práctica diaria o quieres contribuir al proyecto, puedes hacerlo mediante los siguientes canales oficiales:

[![Apóyame en Liberapay](https://liberapay.com/assets/widgets/donate.svg)](https://liberapay.com/ferlagod./)

- 💬 **Contacto y Comunidad:** Sígueme en el Fediverso a través de [Frikiverse (Mastodon)](https://frikiverse.zone/@ferlagod).
- 💻 **Desarrollo:** Consulta el repositorio del código en [ForjaLibre](https://forjalibre.eu/ferlagod/miscuras).

---

*Desarrollado con ❤️ y mucho café por [ferlagod](https://github.com/ferlagod).*
