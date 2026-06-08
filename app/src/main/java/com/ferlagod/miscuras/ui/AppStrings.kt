/*
 * Mis Curas
 * Copyright (C) 2026 Fernando Lago (ferlagod)
 *
 * Este programa es software libre: puede redistribuirlo y/o modificarlo
 * bajo los términos de la Licencia Pública General GNU publicada por
 * la Free Software Foundation, ya sea la versión 3 de la Licencia, o
 * (a su elección) cualquier versión posterior.
 */
package com.ferlagod.miscuras.ui

/**
 * Interfaz que define todos los textos visibles en la aplicación.
 * Facilita la internacionalización y permite cambiar de idioma en tiempo real
 * de manera sencilla usando el patrón Strategy.
 */
sealed interface AppStrings {
    val appName: String
    val disclaimerTitle: String
    val disclaimerText: String
    val acceptButton: String
    val splashSubtitle: String
    
    val bedStateLabel: String
    val bedStateDesc: String
    val exudateLevelLabel: String
    val exudateLevelDesc: String
    val infectionLabel: String
    val woundSizeLabel: String
    val woundSizeDesc: String
    val woundLengthLabel: String
    val woundWidthLabel: String
    val specialLocationLabel: String
    val specialLocationDesc: String
    val locationNone: String
    val locationHeel: String
    val locationSacrum: String
    
    val germLabel: String
    val germDesc: String
    val germNone: String
    val germPseudomonas: String
    val germMRSA: String
    val germCandida: String
    val germAcinetobacter: String
    val germBiofilm: String
    val infectionDisclaimer: String
    
    // Lecho values
    val necrosis: String
    val esfacelo: String
    val granulacion: String
    val epitelizacion: String
    val pielIntacta: String
    
    // Exudado values
    val nulo: String
    val bajo: String
    val moderado: String
    val alto: String
    
    // Tipo Exudado
    val exudateTypeLabel: String
    val exudateTypeDesc: String
    val exuSeroso: String
    val exuTurbio: String
    val exuPurulento: String
    val exuHemorragico: String
    val exuSerohemorragico: String
    
    // Piel Perilesional
    val perilesionalLabel: String
    val perilesionalDesc: String
    val periSana: String
    val periMacerada: String
    val periDescamativa: String
    val periEccematosa: String
    val periEritematosa: String
    
    // Infeccion
    val yes: String
    val no: String
    
    val searchButton: String
    val selectionHeaderTitle: String
    val selectionHeaderSubtitle: String
    val footerText: String
    val infectionDetected: String
    val noInfection: String
    
    // Results
    val resultsTitle: String
    val evaluationDone: String
    val infectionChip: String
    val availableProducts: String
    val clinicalMechanism: String
    val precautionsTitle: String
    val recommendedFamilySingular: String
    val recommendedFamilyPlural: String
    val noMatchTitle: String
    val noMatchSubtitle: String
    val noMatchSubtitle2: String
    val valuationSuggestionsTitle: String
    val suggestion1: String
    val suggestion2: String
    
    // Details dialog
    val codeLabel: String
    val sizeLabel: String
    val descriptionLabel: String
    val interactionsLabel: String
    val primaryUseLabel: String
    val secondaryUseLabel: String
    val closeButton: String
    
    // Settings
    val settingsTitle: String
    val themeLabel: String
    val themeLight: String
    val themeDark: String
    val themeSystem: String
    val languageLabel: String
    val languageEs: String
    val languageEn: String
    val languagePt: String
    val appVersionLabel: String
    val developerLabel: String
    val donationsLabel: String
    val sourceCodeLabel: String
    val donationButtonText: String
    val devProfileText: String
    val exitDialogTitle: String
    val exitDialogText: String
    val exitDialogConfirm: String
    val exitDialogDismiss: String
    
    // Suggest product form
    val suggestProductButton: String
    val nameFieldLabel: String
    val isHealthProLabel: String
    val isLabLabel: String
    val productNameLabel: String
    val productBedLabel: String
    val productExudateLabel: String
    val otherSuggestionsLabel: String
    val suggestProductDialogTitle: String
    val suggestProductTitle: String
    val submitSuggestionButton: String
    val cancelSuggestionButton: String
    val aiAssistantTitle: String
    val aiResponseError: String
    val cancelButton: String
    val sendButton: String
    val formSuccessMsg: String
    val formErrorMsg: String
    val formSendingMsg: String
    val primaryDressingCategory: String
    val secondaryDressingCategory: String
    val arRulerTitle: String
    val arCancelButton: String
    val arInstructionStartLength: String
    val arInstructionEndLength: String
    val arInstructionStartWidth: String
    val arInstructionEndWidth: String
    val arInstructionConfirm: String
    val arRestartButton: String
    val bradenProactiveSuggest: String
    val bradenEvaluateButton: String
    val bradenPreventiveAlert: String

    
    // Additional WoundScreen
    val edgesLabel: String
    val edgesDesc: String
    val copySummaryToast: String
    val copySummaryButton: String

    // GlossaryScreen
    val glossaryTitle: String
    val glossaryBack: String
    val glossaryDescription: String
    val glossaryCollapse: String
    val glossaryExpand: String
    
    // BradenScreen
    val bradenTitle: String
    val bradenBack: String
    val bradenCopiedSnackbar: String
    val bradenOk: String
    val bradenPoints: String
    val bradenRiskLow: String
    val bradenRiskModerate: String
    val bradenRiskHigh: String
    val bradenRiskVeryHigh: String
    val bradenSensory: String
    val bradenSensory1: String
    val bradenSensory2: String
    val bradenSensory3: String
    val bradenSensory4: String
    val bradenMoisture: String
    val bradenMoisture1: String
    val bradenMoisture2: String
    val bradenMoisture3: String
    val bradenMoisture4: String
    val bradenActivity: String
    val bradenActivity1: String
    val bradenActivity2: String
    val bradenActivity3: String
    val bradenActivity4: String
    val bradenMobility: String
    val bradenMobility1: String
    val bradenMobility2: String
    val bradenMobility3: String
    val bradenMobility4: String
    val bradenNutrition: String
    val bradenNutrition1: String
    val bradenNutrition2: String
    val bradenNutrition3: String
    val bradenNutrition4: String
    val bradenFriction: String
    val bradenFriction1: String
    val bradenFriction2: String
    val bradenFriction3: String
    val bradenSuggestedPlan: String
    val bradenCopyClipboard: String
    val bradenRecLow: String
    val bradenRecModerate: String
    val bradenRecHigh: String
    val bradenRecVeryHigh: String

    // Resumen Evolutivo
    val repTimersTitle: String
    val repTissue: String
    val repInfection: String
    val repMoisture: String
    val repEdgesFormat: String
    val repPainFormat: String
    val repSizeFormat: String
    val repLocationFormat: String
    val repBradenTitle: String
    val repBradenScoreFormat: String
    val repBradenPreventiveTitle: String
    val repBradenPreventiveText: String
    val repPlanTitle: String
    val repPending: String
    val repProductTitle: String
    val repInfYesFormat: String
    val repUnspecified: String
    val confirmButton: String
    val settingsSuggestTitle: String
    val settingsSuggestDesc: String

    companion object {
        /**
         * Retorna el objeto contenedor de strings para el idioma especificado.
         * @param lang Código de idioma (e.g., "es", "en", "pt").
         * @return La implementación concreta de [AppStrings].
         */
        fun getStrings(lang: String): AppStrings {
            return when (lang.lowercase()) {
                "en" -> EnStrings
                "pt" -> PtStrings
                else -> EsStrings
            }
        }
        
        /**
         * Traduce un término clínico almacenado en la base de datos al idioma actual
         * para mostrarlo de forma correcta en la interfaz de usuario.
         *
         * @param term El término en la base de datos (normalmente en español).
         * @param lang El idioma al que se desea traducir.
         * @return El término traducido o el término original si no hay traducción.
         */
        fun translateClinicalTerm(term: String, lang: String): String {
            val s = getStrings(lang)
            return when (term.trim()) {
                "Necrosis" -> s.necrosis
                "Esfacelo" -> s.esfacelo
                "Granulación" -> s.granulacion
                "Epitelización" -> s.epitelizacion
                "Piel Intacta (Prevención)" -> s.pielIntacta
                "Nulo" -> s.nulo
                "Bajo" -> s.bajo
                "Moderado" -> s.moderado
                "Alto" -> s.alto
                "Seroso" -> s.exuSeroso
                "Turbio" -> s.exuTurbio
                "Purulento" -> s.exuPurulento
                "Hemorrágico" -> s.exuHemorragico
                "Serohemorrágico" -> s.exuSerohemorragico
                "Sana" -> s.periSana
                "Macerada" -> s.periMacerada
                "Descamativa" -> s.periDescamativa
                "Eccematosa" -> s.periEccematosa
                "Eritematosa" -> s.periEritematosa
                "Ninguno" -> s.locationNone
                "Talón" -> s.locationHeel
                "Sacro" -> s.locationSacrum
                "Desconocido" -> s.germNone
                "Pseudomonas aeruginosa" -> s.germPseudomonas
                "MRSA" -> s.germMRSA
                "Candida albicans" -> s.germCandida
                "Acinetobacter" -> s.germAcinetobacter
                "Biofilm complejo" -> s.germBiofilm
                else -> term // Fallback for specific names
            }
        }

        /**
         * Mapea un término traducido desde la interfaz de usuario
         * de vuelta a su valor original en español usado en la base de datos,
         * para poder realizar búsquedas correctas.
         *
         * @param translatedTerm El término seleccionado en el UI.
         * @return El término original de la base de datos.
         */
        fun mapToDbTerm(translatedTerm: String): String {
            val sEs = getStrings("es")
            val sEn = getStrings("en")
            val sPt = getStrings("pt")
            
            return when (translatedTerm.trim()) {
                sEs.necrosis, sEn.necrosis, sPt.necrosis -> "Necrosis"
                sEs.esfacelo, sEn.esfacelo, sPt.esfacelo -> "Esfacelo"
                sEs.granulacion, sEn.granulacion, sPt.granulacion -> "Granulación"
                sEs.epitelizacion, sEn.epitelizacion, sPt.epitelizacion -> "Epitelización"
                sEs.pielIntacta, sEn.pielIntacta, sPt.pielIntacta -> "Piel Intacta (Prevención)"
                
                sEs.nulo, sEn.nulo, sPt.nulo -> "Nulo"
                sEs.bajo, sEn.bajo, sPt.bajo -> "Bajo"
                sEs.moderado, sEn.moderado, sPt.moderado -> "Moderado"
                sEs.alto, sEn.alto, sPt.alto -> "Alto"
                
                sEs.exuSeroso, sEn.exuSeroso, sPt.exuSeroso -> "Seroso"
                sEs.exuTurbio, sEn.exuTurbio, sPt.exuTurbio -> "Turbio"
                sEs.exuPurulento, sEn.exuPurulento, sPt.exuPurulento -> "Purulento"
                sEs.exuHemorragico, sEn.exuHemorragico, sPt.exuHemorragico -> "Hemorrágico"
                sEs.exuSerohemorragico, sEn.exuSerohemorragico, sPt.exuSerohemorragico -> "Serohemorrágico"

                sEs.periSana, sEn.periSana, sPt.periSana -> "Sana"
                sEs.periMacerada, sEn.periMacerada, sPt.periMacerada -> "Macerada"
                sEs.periDescamativa, sEn.periDescamativa, sPt.periDescamativa -> "Descamativa"
                sEs.periEccematosa, sEn.periEccematosa, sPt.periEccematosa -> "Eccematosa"
                sEs.periEritematosa, sEn.periEritematosa, sPt.periEritematosa -> "Eritematosa"
                
                sEs.locationNone, sEn.locationNone, sPt.locationNone -> "Ninguno"
                sEs.locationHeel, sEn.locationHeel, sPt.locationHeel -> "Talón"
                sEs.locationSacrum, sEn.locationSacrum, sPt.locationSacrum -> "Sacro"
                
                sEs.germNone, sEn.germNone, sPt.germNone -> "Desconocido"
                sEs.germPseudomonas, sEn.germPseudomonas, sPt.germPseudomonas -> "Pseudomonas aeruginosa"
                sEs.germMRSA, sEn.germMRSA, sPt.germMRSA -> "MRSA"
                sEs.germCandida, sEn.germCandida, sPt.germCandida -> "Candida albicans"
                sEs.germAcinetobacter, sEn.germAcinetobacter, sPt.germAcinetobacter -> "Acinetobacter"
                sEs.germBiofilm, sEn.germBiofilm, sPt.germBiofilm -> "Biofilm complejo"
                else -> translatedTerm
            }
        }
    }
}

/** Implementación de [AppStrings] para Español. */
object EsStrings : AppStrings {
    override val appName = "Mis Curas"
    override val disclaimerTitle = "Descargo de Responsabilidad"
    override val disclaimerText = "Esta aplicación es una herramienta de apoyo a la toma de decisiones clínicas y no reemplaza el juicio clínico independiente de un profesional sanitario cualificado. La Inteligencia Artificial proporciona sugerencias orientativas basadas en la información proporcionada; el diagnóstico y tratamiento definitivo son responsabilidad exclusiva del profesional."
    override val acceptButton = "Entendido y Aceptar"
    override val splashSubtitle = "Guía de apósitos para enfermeras"
    
    override val bedStateLabel = "Estado del lecho de la herida"
    override val bedStateDesc = "Tipo de tejido predominante en la herida"
    override val exudateLevelLabel = "Nivel de exudado"
    override val exudateLevelDesc = "Cantidad de fluido que produce la herida"
    override val infectionLabel = "¿Presenta signos de infección?"
    override val woundSizeLabel = "Tamaño de la herida (Opcional)"
    override val woundSizeDesc = "Introduce las medidas para filtrar los apósitos"
    override val woundLengthLabel = "Largo (cm)"
    override val woundWidthLabel = "Ancho (cm)"
    override val specialLocationLabel = "Ubicación especial"
    override val specialLocationDesc = "¿Se encuentra en una zona anatómica específica?"
    override val locationNone = "Ninguna"
    override val locationHeel = "Talón"
    override val locationSacrum = "Sacro"
    
    override val germLabel = "Microorganismo"
    override val germDesc = "¿Se conoce el germen causante?"
    override val germNone = "Desconocido / General"
    override val germPseudomonas = "Pseudomonas aeruginosa"
    override val germMRSA = "Staphylococcus aureus (MRSA)"
    override val germCandida = "Candida albicans"
    override val germAcinetobacter = "Acinetobacter"
    override val germBiofilm = "Biofilm complejo"
    override val infectionDisclaimer = "Nota: Ningún apósito sustituye al desbridamiento cortante o mecánico, que es el paso principal en el control del biofilm y la infección. Los apósitos son coadyuvantes en esta fase de limpieza."
    
    override val necrosis = "Necrosis"
    override val esfacelo = "Esfacelo"
    override val granulacion = "Granulación"
    override val epitelizacion = "Epitelización"
    override val pielIntacta = "Piel Intacta (Prevención)"
    
    override val nulo = "Nulo"
    override val bajo = "Bajo"
    override val moderado = "Moderado"
    override val alto = "Alto"
    
    override val exudateTypeLabel = "Tipo de exudado"
    override val exudateTypeDesc = "Aspecto clínico del fluido de la herida"
    override val exuSeroso = "Seroso"
    override val exuTurbio = "Turbio"
    override val exuPurulento = "Purulento"
    override val exuHemorragico = "Hemorrágico"
    override val exuSerohemorragico = "Serohemorrágico"
    
    override val perilesionalLabel = "Piel perilesional"
    override val perilesionalDesc = "Estado de la piel que rodea a la herida"
    override val periSana = "Sana"
    override val periMacerada = "Macerada"
    override val periDescamativa = "Descamativa"
    override val periEccematosa = "Eccematosa"
    override val periEritematosa = "Eritematosa"
    
    override val yes = "Sí"
    override val no = "No"
    
    override val searchButton = "Buscar Apósitos"
    override val selectionHeaderTitle = "Evaluación de la herida"
    override val selectionHeaderSubtitle = "Selecciona las características de la herida\npara obtener una recomendación de apósito"
    override val footerText = "Basado en las guías clínicas GNEAUPP"
    override val infectionDetected = "Infección detectada"
    override val noInfection = "Sin signos de infección"
    
    override val resultsTitle = "Resultado"
    override val evaluationDone = "Evaluación realizada"
    override val infectionChip = "Infección"
    override val availableProducts = "Productos disponibles"
    override val clinicalMechanism = "Mecanismo de acción"
    override val precautionsTitle = "Interacciones y Precauciones"
    override val recommendedFamilySingular = "Familia recomendada"
    override val recommendedFamilyPlural = "Familias recomendadas"
    override val noMatchTitle = "Sin coincidencia"
    override val noMatchSubtitle = "No se encontró una regla clínica para la combinación:"
    override val noMatchSubtitle2 = "Consulta las guías GNEAUPP o\ncontacta con un profesional especializado"
    override val valuationSuggestionsTitle = "Sugerencias de valoración:"
    override val suggestion1 = "Evalúa la presencia de esfacelos húmedos o tejido necrótico seco que requiera desbridamiento."
    override val suggestion2 = "Si sospechas de infección (calor local, eritema, aumento de exudado, dolor, mal olor), prioriza apósitos con plata o antimicrobianos."
    
    override val codeLabel = "Código CN"
    override val sizeLabel = "Medidas"
    override val descriptionLabel = "Descripción"
    override val interactionsLabel = "Interacciones con otros productos"
    override val primaryUseLabel = "Uso Primario"
    override val secondaryUseLabel = "Uso Secundario"
    override val closeButton = "Cerrar"
    
    override val settingsTitle = "Ajustes"
    override val themeLabel = "Tema de la aplicación"
    override val themeLight = "Claro"
    override val themeDark = "Oscuro"
    override val themeSystem = "Sistema"
    override val languageLabel = "Idioma"
    override val languageEs = "Español"
    override val languageEn = "English"
    override val languagePt = "Português"
    override val appVersionLabel = "Versión"
    override val developerLabel = "Sobre el desarrollador"
    override val donationsLabel = "Apoyar el proyecto (Donaciones)"
    override val sourceCodeLabel = "Código fuente"
    override val donationButtonText = "Donar en Liberapay"
    override val devProfileText = "Ver perfil en Frikiverse"
    override val exitDialogTitle = "Salir de la aplicación"
    override val exitDialogText = "¿Estás seguro de que quieres salir?"
    override val exitDialogConfirm = "Sí"
    override val exitDialogDismiss = "No"
    
    override val suggestProductButton = "¿No encuentras un apósito? Sugiérelo"
    override val nameFieldLabel = "Tu nombre"
    override val isHealthProLabel = "¿Eres profesional sanitario?"
    override val isLabLabel = "¿Perteneces a un laboratorio?"
    override val productNameLabel = "Nombre del producto a sugerir"
    override val productBedLabel = "Lecho indicado"
    override val productExudateLabel = "Exudado indicado"
    override val otherSuggestionsLabel = "Otras sugerencias"
    override val suggestProductDialogTitle = "Sugerir nuevo producto"
    override val suggestProductTitle = "Sugerir nuevo producto"
    override val submitSuggestionButton = "Enviar sugerencia"
    override val cancelSuggestionButton = "Cancelar"
    override val aiAssistantTitle = "Asistente Educativo"
    override val aiResponseError = "No se pudo obtener la respuesta."
    override val cancelButton = "Cancelar"
    override val sendButton = "Enviar"
    override val formSuccessMsg = "Sugerencia enviada correctamente. ¡Gracias!"
    override val formErrorMsg = "Error al enviar. Comprueba tu conexión."
    override val formSendingMsg = "Enviando..."
    override val primaryDressingCategory = "Apósito Primario (Contacto con lecho)"
    override val secondaryDressingCategory = "Apósito Secundario (Cobertura/Fijación)"
    override val arRulerTitle = "Regla AR"
    override val arCancelButton = "Cancelar"
    override val arInstructionStartLength = "Mueve el móvil para detectar la superficie.\nArrastra el dedo para fijar el inicio del LARGO."
    override val arInstructionEndLength = "Arrastra el dedo para fijar el fin del LARGO."
    override val arInstructionStartWidth = "Largo: %s cm.\nArrastra el dedo para fijar el inicio del ANCHO."
    override val arInstructionEndWidth = "Arrastra el dedo para fijar el fin del ANCHO."
    override val arInstructionConfirm = "Largo: %s cm | Ancho: %s cm.\nPulsa Confirmar."
    override val arRestartButton = "Reiniciar"
    override val bradenProactiveSuggest = "¿Deseas realizar la valoración de riesgo de Braden para este paciente?"
    override val bradenEvaluateButton = "Evaluar Escala Braden"
    override val bradenPreventiveAlert = "Alerta Preventiva Braden (%s)"

    override val edgesLabel = "Bordes de la herida"
    override val edgesDesc = "Estado de los márgenes de la lesión"
    override val copySummaryToast = "Resumen evolutivo copiado"
    override val copySummaryButton = "Copiar para Evolutivo Hospitalario"

    override val glossaryTitle = "Biblioteca Clínica GNEAUPP"
    override val glossaryBack = "Volver a la calculadora"
    override val glossaryDescription = "Glosario de consulta rápida para estudiantes y profesionales. Toda la información está basada en los estándares del Grupo Nacional para el Estudio y Asesoramiento en Úlceras por Presión y Heridas Crónicas."
    override val glossaryCollapse = "Colapsar"
    override val glossaryExpand = "Expandir"

    override val bradenTitle = "Escala de Braden"
    override val bradenBack = "Volver"
    override val bradenCopiedSnackbar = "Plan preventivo copiado al portapapeles"
    override val bradenOk = "OK"
    override val bradenPoints = "Puntos"
    override val bradenRiskLow = "Riesgo Bajo / Sin Riesgo"
    override val bradenRiskModerate = "Riesgo Moderado"
    override val bradenRiskHigh = "Riesgo Alto"
    override val bradenRiskVeryHigh = "Riesgo Muy Alto"
    override val bradenSensory = "Percepción Sensorial"
    override val bradenSensory1 = "Completamente limitada (1)"
    override val bradenSensory2 = "Muy limitada (2)"
    override val bradenSensory3 = "Ligeramente limitada (3)"
    override val bradenSensory4 = "Sin limitación (4)"
    override val bradenMoisture = "Exposición a la Humedad"
    override val bradenMoisture1 = "Constantemente húmeda (1)"
    override val bradenMoisture2 = "A menudo húmeda (2)"
    override val bradenMoisture3 = "Ocasionalmente húmeda (3)"
    override val bradenMoisture4 = "Raramente húmeda (4)"
    override val bradenActivity = "Actividad"
    override val bradenActivity1 = "Encamado (1)"
    override val bradenActivity2 = "En silla (2)"
    override val bradenActivity3 = "Deambula ocasionalmente (3)"
    override val bradenActivity4 = "Deambula frecuentemente (4)"
    override val bradenMobility = "Movilidad"
    override val bradenMobility1 = "Completamente inmóvil (1)"
    override val bradenMobility2 = "Muy limitada (2)"
    override val bradenMobility3 = "Ligeramente limitada (3)"
    override val bradenMobility4 = "Sin limitaciones (4)"
    override val bradenNutrition = "Nutrición"
    override val bradenNutrition1 = "Muy pobre (1)"
    override val bradenNutrition2 = "Probablemente inadecuada (2)"
    override val bradenNutrition3 = "Adecuada (3)"
    override val bradenNutrition4 = "Excelente (4)"
    override val bradenFriction = "Roce y Peligro de Lesiones"
    override val bradenFriction1 = "Problema (1)"
    override val bradenFriction2 = "Problema potencial (2)"
    override val bradenFriction3 = "No hay problema aparente (3)"
    override val bradenSuggestedPlan = "Plan Preventivo Sugerido:"
    override val bradenCopyClipboard = "Copiar al Portapapeles"
    override val bradenRecLow = "• Cuidados básicos de la piel.\n• Fomentar la movilidad."
    override val bradenRecModerate = "• Cambios posturales regulares.\n• Aplicación de Ácidos Grasos Hiperoxigenados (AGHO).\n• Vigilancia estrecha de puntos de apoyo."
    override val bradenRecHigh = "• Cambios posturales cada 2-3 horas.\n• Uso de Superficies Especiales de Manejo de la Presión (SEMP) estáticas/dinámicas.\n• AGHO diarios.\n• Suplementación nutricional si procede."
    override val bradenRecVeryHigh = "• Cambios posturales estrictos cada 2 horas.\n• Uso de SEMP dinámicas de alta gama (colchón de aire alternante).\n• Elevación de talones.\n• Protección proactiva con apósitos multicapa."

    override val repTimersTitle = "[VALORACIÓN DE HERIDA - CRITERIOS TIMERS]"
    override val repTissue = "- Tejido (T): "
    override val repInfection = "- Infección/Inflamación (I): "
    override val repMoisture = "- Exudado/Humedad (M): "
    override val repEdgesFormat = "- Bordes y Perilesional (E): Bordes %s / Piel %s"
    override val repPainFormat = "- Sensibilidad/Dolor (S): %s/10"
    override val repSizeFormat = "- Tamaño: %s"
    override val repLocationFormat = "- Localización: %s"
    override val repBradenTitle = "[ESCALA BRADEN]"
    override val repBradenScoreFormat = "- Puntuación: %s/23 (%s)"
    override val repBradenPreventiveTitle = "[PREVENCIÓN ALTO RIESGO LPP]"
    override val repBradenPreventiveText = "- Ácidos Grasos Hiperoxigenados (AGHO).\n- Espumas de poliuretano sacras/talonares de 5 capas.\n- Superficies Especiales de Manejo de la Presión (SEMP) / Colchón de aire alternante."
    override val repPlanTitle = "[PLAN TERAPÉUTICO PROPUESTO (GNEAUPP)]"
    override val repPending = "Pendiente de análisis clínico."
    override val repProductTitle = "[PRODUCTO LOCAL SELECCIONADO]"
    override val repInfYesFormat = "Sí (Sospecha/Confirmado: %s)"
    override val repUnspecified = "No especificado"
    override val confirmButton = "Confirmar"
    override val settingsSuggestTitle = "Enviar sugerencias (requiere internet)"
    override val settingsSuggestDesc = "Sugiere un apósito que no encuentres. Solo se enviará la información del producto y tu nombre (opcional), ningún dato de pacientes sale del dispositivo."
}

/** Implementación de [AppStrings] para Inglés. */
object EnStrings : AppStrings {
    override val appName = "My Cures"
    override val disclaimerTitle = "Disclaimer"
    override val disclaimerText = "This application is a clinical decision support tool and does not replace the independent clinical judgment of a qualified healthcare professional. Artificial Intelligence provides orientative suggestions based on the information provided; definitive diagnosis and treatment are the exclusive responsibility of the professional."
    override val acceptButton = "Understood & Accept"
    override val splashSubtitle = "Dressing guide for nurses"
    
    override val bedStateLabel = "Wound bed status"
    override val bedStateDesc = "Predominant tissue type in the wound"
    override val exudateLevelLabel = "Exudate level"
    override val exudateLevelDesc = "Amount of fluid produced by the wound"
    override val infectionLabel = "Does it show signs of infection?"
    override val woundSizeLabel = "Wound size (Optional)"
    override val woundSizeDesc = "Enter dimensions to filter dressings"
    override val woundLengthLabel = "Length (cm)"
    override val woundWidthLabel = "Width (cm)"
    override val specialLocationLabel = "Special location"
    override val specialLocationDesc = "Is it located in a specific anatomical area?"
    override val locationNone = "None"
    override val locationHeel = "Heel"
    override val locationSacrum = "Sacrum"
    
    override val germLabel = "Microorganism"
    override val germDesc = "Is the causative germ known?"
    override val germNone = "Unknown / General"
    override val germPseudomonas = "Pseudomonas aeruginosa"
    override val germMRSA = "Staphylococcus aureus (MRSA)"
    override val germCandida = "Candida albicans"
    override val germAcinetobacter = "Acinetobacter"
    override val germBiofilm = "Complex biofilm"
    override val infectionDisclaimer = "Note: No dressing replaces sharp or mechanical debridement, which is the main step in controlling biofilm and infection. Dressings are adjunctive in this cleaning phase."
    
    override val necrosis = "Necrosis"
    override val esfacelo = "Slough"
    override val granulacion = "Granulation"
    override val epitelizacion = "Epithelialization"
    override val pielIntacta = "Intact Skin (Prevention)"
    
    override val nulo = "None"
    override val bajo = "Low"
    override val moderado = "Moderate"
    override val alto = "High"
    
    override val exudateTypeLabel = "Exudate type"
    override val exudateTypeDesc = "Clinical appearance of wound fluid"
    override val exuSeroso = "Serous"
    override val exuTurbio = "Cloudy"
    override val exuPurulento = "Purulent"
    override val exuHemorragico = "Hemorrhagic"
    override val exuSerohemorragico = "Serosanguineous"
    
    override val perilesionalLabel = "Periwound skin"
    override val perilesionalDesc = "Condition of the skin surrounding the wound"
    override val periSana = "Healthy"
    override val periMacerada = "Macerated"
    override val periDescamativa = "Desquamating"
    override val periEccematosa = "Eczematous"
    override val periEritematosa = "Erythematous"
    
    override val yes = "Yes"
    override val no = "No"
    
    override val searchButton = "Search Dressings"
    override val selectionHeaderTitle = "Wound Assessment"
    override val selectionHeaderSubtitle = "Select the wound characteristics\nto get a dressing recommendation"
    override val footerText = "Based on GNEAUPP clinical guidelines"
    override val infectionDetected = "Infection detected"
    override val noInfection = "No signs of infection"
    
    override val resultsTitle = "Result"
    override val evaluationDone = "Assessment completed"
    override val infectionChip = "Infection"
    override val availableProducts = "Available products"
    override val clinicalMechanism = "Mechanism of action"
    override val precautionsTitle = "Interactions and Precautions"
    override val recommendedFamilySingular = "Recommended family"
    override val recommendedFamilyPlural = "Recommended families"
    override val noMatchTitle = "No Match"
    override val noMatchSubtitle = "No clinical rule found for the combination:"
    override val noMatchSubtitle2 = "Consult GNEAUPP guidelines or\ncontact a specialized professional"
    override val valuationSuggestionsTitle = "Assessment suggestions:"
    override val suggestion1 = "Evaluate the presence of wet slough or dry necrotic tissue requiring debridement."
    override val suggestion2 = "If infection is suspected (local heat, erythema, increased exudate, pain, bad odor), prioritize silver or antimicrobial dressings."
    
    override val codeLabel = "CN Code"
    override val sizeLabel = "Dimensions"
    override val descriptionLabel = "Description"
    override val interactionsLabel = "Interactions with other products"
    override val primaryUseLabel = "Primary Use"
    override val secondaryUseLabel = "Secondary Use"
    override val closeButton = "Close"
    
    override val settingsTitle = "Settings"
    override val themeLabel = "App Theme"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val themeSystem = "System"
    override val languageLabel = "Language"
    override val languageEs = "Español"
    override val languageEn = "English"
    override val languagePt = "Português"
    override val appVersionLabel = "Version"
    override val developerLabel = "About the developer"
    override val donationsLabel = "Support the project (Donations)"
    override val sourceCodeLabel = "Source code"
    override val donationButtonText = "Donate on Liberapay"
    override val devProfileText = "View profile on Frikiverse"
    override val exitDialogTitle = "Exit application"
    override val exitDialogText = "Are you sure you want to exit?"
    override val exitDialogConfirm = "Yes"
    override val exitDialogDismiss = "No"
    
    override val suggestProductButton = "Can't find a dressing? Suggest it"
    override val nameFieldLabel = "Your name"
    override val isHealthProLabel = "Are you a healthcare professional?"
    override val isLabLabel = "Do you belong to a laboratory?"
    override val productNameLabel = "Name of the product to suggest"
    override val productBedLabel = "Indicated wound bed"
    override val productExudateLabel = "Indicated exudate"
    override val otherSuggestionsLabel = "Other suggestions"
    override val suggestProductDialogTitle = "Suggest new product"
    override val suggestProductTitle = "Suggest new product"
    override val submitSuggestionButton = "Submit suggestion"
    override val cancelSuggestionButton = "Cancel"
    override val aiAssistantTitle = "Educational Assistant"
    override val aiResponseError = "Could not retrieve the response."
    override val cancelButton = "Cancel"
    override val sendButton = "Send"
    override val formSuccessMsg = "Suggestion sent successfully. Thank you!"
    override val formErrorMsg = "Error sending. Check your connection."
    override val formSendingMsg = "Sending..."
    override val primaryDressingCategory = "Primary Dressing (Wound contact)"
    override val secondaryDressingCategory = "Secondary Dressing (Cover/Fixation)"
    override val arRulerTitle = "AR Ruler"
    override val arCancelButton = "Cancel"
    override val arInstructionStartLength = "Move device to detect surface.\nDrag to set the start of the LENGTH."
    override val arInstructionEndLength = "Drag to set the end of the LENGTH."
    override val arInstructionStartWidth = "Length: %s cm.\nDrag to set the start of the WIDTH."
    override val arInstructionEndWidth = "Drag to set the end of the WIDTH."
    override val arInstructionConfirm = "Length: %s cm | Width: %s cm.\nPress Confirm."
    override val arRestartButton = "Restart"
    override val bradenProactiveSuggest = "Do you want to perform a Braden risk assessment for this patient?"
    override val bradenEvaluateButton = "Evaluate Braden Scale"
    override val bradenPreventiveAlert = "Braden Preventive Alert (%s)"

    override val edgesLabel = "Wound edges"
    override val edgesDesc = "Condition of the lesion margins"
    override val copySummaryToast = "Clinical summary copied"
    override val copySummaryButton = "Copy for Clinical Report"

    override val glossaryTitle = "GNEAUPP Clinical Library"
    override val glossaryBack = "Back to calculator"
    override val glossaryDescription = "Quick reference glossary for students and professionals. All information is based on the standards of the National Group for the Study and Advice on Pressure Ulcers and Chronic Wounds."
    override val glossaryCollapse = "Collapse"
    override val glossaryExpand = "Expand"

    override val bradenTitle = "Braden Scale"
    override val bradenBack = "Back"
    override val bradenCopiedSnackbar = "Preventive plan copied to clipboard"
    override val bradenOk = "OK"
    override val bradenPoints = "Points"
    override val bradenRiskLow = "Low Risk / No Risk"
    override val bradenRiskModerate = "Moderate Risk"
    override val bradenRiskHigh = "High Risk"
    override val bradenRiskVeryHigh = "Very High Risk"
    override val bradenSensory = "Sensory Perception"
    override val bradenSensory1 = "Completely limited (1)"
    override val bradenSensory2 = "Very limited (2)"
    override val bradenSensory3 = "Slightly limited (3)"
    override val bradenSensory4 = "No impairment (4)"
    override val bradenMoisture = "Moisture Exposure"
    override val bradenMoisture1 = "Constantly moist (1)"
    override val bradenMoisture2 = "Very moist (2)"
    override val bradenMoisture3 = "Occasionally moist (3)"
    override val bradenMoisture4 = "Rarely moist (4)"
    override val bradenActivity = "Activity"
    override val bradenActivity1 = "Bedfast (1)"
    override val bradenActivity2 = "Chairfast (2)"
    override val bradenActivity3 = "Walks occasionally (3)"
    override val bradenActivity4 = "Walks frequently (4)"
    override val bradenMobility = "Mobility"
    override val bradenMobility1 = "Completely immobile (1)"
    override val bradenMobility2 = "Very limited (2)"
    override val bradenMobility3 = "Slightly limited (3)"
    override val bradenMobility4 = "No limitations (4)"
    override val bradenNutrition = "Nutrition"
    override val bradenNutrition1 = "Very poor (1)"
    override val bradenNutrition2 = "Probably inadequate (2)"
    override val bradenNutrition3 = "Adequate (3)"
    override val bradenNutrition4 = "Excellent (4)"
    override val bradenFriction = "Friction and Shear"
    override val bradenFriction1 = "Problem (1)"
    override val bradenFriction2 = "Potential problem (2)"
    override val bradenFriction3 = "No apparent problem (3)"
    override val bradenSuggestedPlan = "Suggested Preventive Plan:"
    override val bradenCopyClipboard = "Copy to Clipboard"
    override val bradenRecLow = "• Basic skin care.\n• Encourage mobility."
    override val bradenRecModerate = "• Regular positional changes.\n• Application of Hyperoxygenated Fatty Acids (HOFA).\n• Close monitoring of pressure points."
    override val bradenRecHigh = "• Positional changes every 2-3 hours.\n• Use of static/dynamic pressure-relieving surfaces.\n• Daily HOFA.\n• Nutritional supplementation if applicable."
    override val bradenRecVeryHigh = "• Strict positional changes every 2 hours.\n• Use of high-end dynamic pressure-relieving surfaces (alternating air mattress).\n• Heel elevation.\n• Proactive protection with multi-layer dressings."

    override val repTimersTitle = "[WOUND ASSESSMENT - TIMERS CRITERIA]"
    override val repTissue = "- Tissue (T): "
    override val repInfection = "- Infection/Inflammation (I): "
    override val repMoisture = "- Moisture/Exudate (M): "
    override val repEdgesFormat = "- Edges and Periwound (E): Edges %s / Skin %s"
    override val repPainFormat = "- Sensitivity/Pain (S): %s/10"
    override val repSizeFormat = "- Size: %s"
    override val repLocationFormat = "- Location: %s"
    override val repBradenTitle = "[BRADEN SCALE]"
    override val repBradenScoreFormat = "- Score: %s/23 (%s)"
    override val repBradenPreventiveTitle = "[HIGH RISK PU PREVENTION]"
    override val repBradenPreventiveText = "- Hyperoxygenated Fatty Acids (HOFA).\n- 5-layer sacral/heel polyurethane foams.\n- Pressure-relieving surfaces / Alternating air mattress."
    override val repPlanTitle = "[PROPOSED THERAPEUTIC PLAN (GNEAUPP)]"
    override val repPending = "Pending clinical analysis."
    override val repProductTitle = "[SELECTED LOCAL PRODUCT]"
    override val repInfYesFormat = "Yes (Suspected/Confirmed: %s)"
    override val repUnspecified = "Not specified"
    override val confirmButton = "Confirm"
    override val settingsSuggestTitle = "Send suggestions (requires internet)"
    override val settingsSuggestDesc = "Suggest a dressing you can't find. Only the product info and your name (optional) will be sent. No patient data leaves the device."
}

/** Implementación de [AppStrings] para Portugués. */
object PtStrings : AppStrings {
    override val appName = "Minhas Curas"
    override val disclaimerTitle = "Aviso de Responsabilidade"
    override val disclaimerText = "Este aplicativo é uma ferramenta de apoio à decisão clínica e não substitui o julgamento clínico independente de um profissional de saúde qualificado. A Inteligência Artificial fornece sugestões orientativas baseadas nas informações fornecidas; o diagnóstico e tratamento definitivos são de responsabilidade exclusiva do profissional."
    override val acceptButton = "Entendido e Aceito"
    override val splashSubtitle = "Guia de curativos para enfermeiras"
    
    override val bedStateLabel = "Estado do leito da ferida"
    override val bedStateDesc = "Tipo de tecido predominante na ferida"
    override val exudateLevelLabel = "Nível de exsudato"
    override val exudateLevelDesc = "Quantidade de fluido produzida pela ferida"
    override val infectionLabel = "Apresenta sinais de infecção?"
    override val woundSizeLabel = "Tamanho da ferida (Opcional)"
    override val woundSizeDesc = "Insira as dimensões para filtrar os curativos"
    override val woundLengthLabel = "Comprimento (cm)"
    override val woundWidthLabel = "Largura (cm)"
    override val specialLocationLabel = "Localização especial"
    override val specialLocationDesc = "Está localizada em uma área anatômica específica?"
    override val locationNone = "Nenhuma"
    override val locationHeel = "Calcanhar"
    override val locationSacrum = "Sacro"
    
    override val germLabel = "Micro-organismo"
    override val germDesc = "O germe causador é conhecido?"
    override val germNone = "Desconhecido / Geral"
    override val germPseudomonas = "Pseudomonas aeruginosa"
    override val germMRSA = "Staphylococcus aureus (MRSA)"
    override val germCandida = "Candida albicans"
    override val germAcinetobacter = "Acinetobacter"
    override val germBiofilm = "Biofilme complexo"
    override val infectionDisclaimer = "Nota: Nenhum curativo substitui o desbridamento cortante ou mecânico, que é a principal etapa no controle do biofilme e da infecção. Os curativos são coadjuvantes nesta fase de limpeza."
    
    override val necrosis = "Necrose"
    override val esfacelo = "Esfacelo"
    override val granulacion = "Granulação"
    override val epitelizacion = "Epitelização"
    override val pielIntacta = "Pele Intacta (Prevenção)"
    
    override val nulo = "Nulo"
    override val bajo = "Baixo"
    override val moderado = "Moderado"
    override val alto = "Alto"
    
    override val exudateTypeLabel = "Tipo de exsudato"
    override val exudateTypeDesc = "Aparência clínica do fluido da ferida"
    override val exuSeroso = "Seroso"
    override val exuTurbio = "Turvo"
    override val exuPurulento = "Purulento"
    override val exuHemorragico = "Hemorrágico"
    override val exuSerohemorragico = "Sero-hemorrágico"
    
    override val perilesionalLabel = "Pele perilesional"
    override val perilesionalDesc = "Condição da pele que circunda a ferida"
    override val periSana = "Saudável"
    override val periMacerada = "Macerada"
    override val periDescamativa = "Descamativa"
    override val periEccematosa = "Eczematosa"
    override val periEritematosa = "Eritematosa"
    
    override val yes = "Sim"
    override val no = "Não"
    
    override val searchButton = "Buscar Curativos"
    override val selectionHeaderTitle = "Avaliação da ferida"
    override val selectionHeaderSubtitle = "Selecione as características da ferida\npara obter uma recomendação de curativo"
    override val footerText = "Baseado nas diretrizes clínicas GNEAUPP"
    override val infectionDetected = "Infecção detectada"
    override val noInfection = "Sem sinais de infecção"
    
    override val resultsTitle = "Resultado"
    override val evaluationDone = "Avaliação realizada"
    override val infectionChip = "Infecção"
    override val availableProducts = "Produtos disponíveis"
    override val clinicalMechanism = "Mecanismo de ação"
    override val precautionsTitle = "Interações e Precauções"
    override val recommendedFamilySingular = "Família recomendada"
    override val recommendedFamilyPlural = "Famílias recomendadas"
    override val noMatchTitle = "Sem correspondência"
    override val noMatchSubtitle = "Não foi encontrada uma regra clínica para a combinação:"
    override val noMatchSubtitle2 = "Consulte as diretrizes da GNEAUPP ou\ncontacte um profissional especializado"
    override val valuationSuggestionsTitle = "Sugestões de avaliação:"
    override val suggestion1 = "Avalie a presença de esfacelo úmido ou tecido necrótico seco que necessite de desbridamento."
    override val suggestion2 = "Se houver suspeita de infecção (calor local, eritema, aumento de exsudato, dor, mau odor), priorize curativos com prata ou antimicrobianos."
    
    override val codeLabel = "Código CN"
    override val sizeLabel = "Medidas"
    override val descriptionLabel = "Descrição"
    override val interactionsLabel = "Interações com outros produtos"
    override val primaryUseLabel = "Uso Primário"
    override val secondaryUseLabel = "Uso Secundário"
    override val closeButton = "Fechar"
    
    override val settingsTitle = "Ajustes"
    override val themeLabel = "Tema do aplicativo"
    override val themeLight = "Claro"
    override val themeDark = "Escuro"
    override val themeSystem = "Sistema"
    override val languageLabel = "Idioma"
    override val languageEs = "Español"
    override val languageEn = "English"
    override val languagePt = "Português"
    override val appVersionLabel = "Versão"
    override val developerLabel = "Sobre o desenvolvedor"
    override val donationsLabel = "Apoiar o projeto (Doações)"
    override val sourceCodeLabel = "Código fonte"
    override val donationButtonText = "Doar no Liberapay"
    override val devProfileText = "Ver perfil no Frikiverse"
    override val exitDialogTitle = "Sair do aplicativo"
    override val exitDialogText = "Tem certeza de que deseja sair?"
    override val exitDialogConfirm = "Sim"
    override val exitDialogDismiss = "Não"
    
    override val suggestProductButton = "Não encontra um curativo? Sugira-o"
    override val nameFieldLabel = "Seu nome"
    override val isHealthProLabel = "Você é profissional de saúde?"
    override val isLabLabel = "Você pertence a um laboratório?"
    override val productNameLabel = "Nome do produto a sugerir"
    override val productBedLabel = "Leito indicado"
    override val productExudateLabel = "Exsudato indicado"
    override val otherSuggestionsLabel = "Outras sugestões"
    override val suggestProductDialogTitle = "Sugerir novo produto"
    override val suggestProductTitle = "Sugerir novo produto"
    override val submitSuggestionButton = "Enviar sugestão"
    override val cancelSuggestionButton = "Cancelar"
    override val aiAssistantTitle = "Assistente Educativo"
    override val aiResponseError = "Não foi possível obter a resposta."
    override val cancelButton = "Cancelar"
    override val sendButton = "Enviar"
    override val formSuccessMsg = "Sugestão enviada com sucesso. Obrigado!"
    override val formErrorMsg = "Erro ao enviar. Verifique sua conexão."
    override val formSendingMsg = "Enviando..."
    override val primaryDressingCategory = "Curativo Primário (Contato com o leito)"
    override val secondaryDressingCategory = "Curativo Secundário (Cobertura/Fixação)"
    override val arRulerTitle = "Régua AR"
    override val arCancelButton = "Cancelar"
    override val arInstructionStartLength = "Mova o dispositivo para detectar a superfície.\nArraste para fixar o início do COMPRIMENTO."
    override val arInstructionEndLength = "Arraste para fixar o fim do COMPRIMENTO."
    override val arInstructionStartWidth = "Comprimento: %s cm.\nArraste para fixar o início da LARGURA."
    override val arInstructionEndWidth = "Arraste para fixar o fim da LARGURA."
    override val arInstructionConfirm = "Comprimento: %s cm | Largura: %s cm.\nPressione Confirmar."
    override val arRestartButton = "Reiniciar"
    override val bradenProactiveSuggest = "Deseja realizar a avaliação de risco de Braden para este paciente?"
    override val bradenEvaluateButton = "Avaliar Escala de Braden"
    override val bradenPreventiveAlert = "Alerta Preventivo Braden (%s)"

    override val edgesLabel = "Bordas da ferida"
    override val edgesDesc = "Condição das margens da lesão"
    override val copySummaryToast = "Resumo clínico copiado"
    override val copySummaryButton = "Copiar para Relatório Clínico"

    override val glossaryTitle = "Biblioteca Clínica GNEAUPP"
    override val glossaryBack = "Voltar à calculadora"
    override val glossaryDescription = "Glossário de referência rápida para estudantes e profissionais. Todas as informações são baseadas nas normas do Grupo Nacional para o Estudo e Aconselhamento de Úlceras por Pressão e Feridas Crônicas."
    override val glossaryCollapse = "Recolher"
    override val glossaryExpand = "Expandir"

    override val bradenTitle = "Escala de Braden"
    override val bradenBack = "Voltar"
    override val bradenCopiedSnackbar = "Plano preventivo copiado para a área de transferência"
    override val bradenOk = "OK"
    override val bradenPoints = "Pontos"
    override val bradenRiskLow = "Risco Baixo / Sem Risco"
    override val bradenRiskModerate = "Risco Moderado"
    override val bradenRiskHigh = "Risco Alto"
    override val bradenRiskVeryHigh = "Risco Muito Alto"
    override val bradenSensory = "Percepção Sensorial"
    override val bradenSensory1 = "Completamente limitada (1)"
    override val bradenSensory2 = "Muito limitada (2)"
    override val bradenSensory3 = "Ligeiramente limitada (3)"
    override val bradenSensory4 = "Sem limitação (4)"
    override val bradenMoisture = "Exposição à Umidade"
    override val bradenMoisture1 = "Constantemente úmida (1)"
    override val bradenMoisture2 = "Muito úmida (2)"
    override val bradenMoisture3 = "Ocasionalmente úmida (3)"
    override val bradenMoisture4 = "Raramente úmida (4)"
    override val bradenActivity = "Atividade"
    override val bradenActivity1 = "Acamado (1)"
    override val bradenActivity2 = "Confinado à cadeira (2)"
    override val bradenActivity3 = "Caminha ocasionalmente (3)"
    override val bradenActivity4 = "Caminha frequentemente (4)"
    override val bradenMobility = "Mobilidade"
    override val bradenMobility1 = "Completamente imóvel (1)"
    override val bradenMobility2 = "Muito limitada (2)"
    override val bradenMobility3 = "Ligeiramente limitada (3)"
    override val bradenMobility4 = "Sem limitações (4)"
    override val bradenNutrition = "Nutrição"
    override val bradenNutrition1 = "Muito pobre (1)"
    override val bradenNutrition2 = "Provavelmente inadequada (2)"
    override val bradenNutrition3 = "Adequada (3)"
    override val bradenNutrition4 = "Excelente (4)"
    override val bradenFriction = "Fricção e Cisalhamento"
    override val bradenFriction1 = "Problema (1)"
    override val bradenFriction2 = "Problema potencial (2)"
    override val bradenFriction3 = "Nenhum problema aparente (3)"
    override val bradenSuggestedPlan = "Plano Preventivo Sugerido:"
    override val bradenCopyClipboard = "Copiar para Área de Transferência"
    override val bradenRecLow = "• Cuidados básicos da pele.\n• Incentivar a mobilidade."
    override val bradenRecModerate = "• Mudanças posturais regulares.\n• Aplicação de Ácidos Graxos Hiperoxigenados (AGHO).\n• Vigilância rigorosa de pontos de pressão."
    override val bradenRecHigh = "• Mudanças posturais a cada 2-3 horas.\n• Uso de superfícies especiais de alívio de pressão (SEMP) estáticas/dinâmicas.\n• AGHO diários.\n• Suplementação nutricional, se aplicável."
    override val bradenRecVeryHigh = "• Mudanças posturais rigorosas a cada 2 horas.\n• Uso de SEMP dinâmicas de alta tecnologia (colchão de ar alternado).\n• Elevação dos calcanhares.\n• Proteção proativa com curativos multicamadas."

    override val repTimersTitle = "[AVALIAÇÃO DE FERIDA - CRITÉRIOS TIMERS]"
    override val repTissue = "- Tecido (T): "
    override val repInfection = "- Infecção/Inflamação (I): "
    override val repMoisture = "- Umidade/Exsudato (M): "
    override val repEdgesFormat = "- Bordas e Perilesional (E): Bordas %s / Pele %s"
    override val repPainFormat = "- Sensibilidade/Dor (S): %s/10"
    override val repSizeFormat = "- Tamanho: %s"
    override val repLocationFormat = "- Localização: %s"
    override val repBradenTitle = "[ESCALA BRADEN]"
    override val repBradenScoreFormat = "- Pontuação: %s/23 (%s)"
    override val repBradenPreventiveTitle = "[PREVENÇÃO ALTO RISCO LPP]"
    override val repBradenPreventiveText = "- Ácidos Graxos Hiperoxigenados (AGHO).\n- Espumas de poliuretano sacrais/calcâneas de 5 camadas.\n- Superfícies Especiais de Manejo da Pressão (SEMP) / Colchão de ar alternado."
    override val repPlanTitle = "[PLANO TERAPÊUTICO PROPOSTO (GNEAUPP)]"
    override val repPending = "Pendente de análise clínica."
    override val repProductTitle = "[PRODUTO LOCAL SELECIONADO]"
    override val repInfYesFormat = "Sim (Suspeita/Confirmado: %s)"
    override val repUnspecified = "Não especificado"
    override val confirmButton = "Confirmar"
    override val settingsSuggestTitle = "Enviar sugestões (requer internet)"
    override val settingsSuggestDesc = "Sugira um curativo que você não encontre. Apenas as informações do produto e seu nome (opcional) serão enviados. Nenhum dado do paciente sai do dispositivo."
}
