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
    val suggestProductDialogTitle: String
    val nameFieldLabel: String
    val isHealthProLabel: String
    val isLabLabel: String
    val productNameLabel: String
    val productBedLabel: String
    val productExudateLabel: String
    val otherSuggestionsLabel: String
    val cancelButton: String
    val sendButton: String
    val formSuccessMsg: String
    val formErrorMsg: String
    val formSendingMsg: String
    
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
    override val disclaimerText = """
        La aplicación "Mis Curas" es una herramienta de apoyo a la toma de decisiones dirigida exclusivamente a profesionales de enfermería. Las recomendaciones sugeridas se fundamentan en las directrices y documentos de consenso de la GNEAUPP (Grupo Nacional para el Estudio y Asesoramiento en Úlceras por Presión y Heridas Crónicas) y en el catálogo habitual de productos del Servicio Andaluz de Salud (SAS).
        
        Esta aplicación no sustituye el juicio clínico, la valoración directa de la herida ni la evaluación integral del paciente. La selección final del apósito o tratamiento es responsabilidad exclusiva del profesional sanitario a cargo, quien debe considerar las características individuales de cada caso.
        
        El desarrollador no asume ninguna responsabilidad por decisiones clínicas tomadas con base en la información proporcionada en esta aplicación, ni por la evolución, complicaciones o resultados derivados de la aplicación de los tratamientos aquí sugeridos.
    """.trimIndent()
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
    override val suggestProductDialogTitle = "Sugerir nuevo producto"
    override val nameFieldLabel = "Tu nombre"
    override val isHealthProLabel = "¿Eres profesional sanitario?"
    override val isLabLabel = "¿Perteneces a un laboratorio?"
    override val productNameLabel = "Nombre del producto a sugerir"
    override val productBedLabel = "Lecho indicado"
    override val productExudateLabel = "Exudado indicado"
    override val otherSuggestionsLabel = "Otras sugerencias"
    override val cancelButton = "Cancelar"
    override val sendButton = "Enviar"
    override val formSuccessMsg = "Sugerencia enviada correctamente. ¡Gracias!"
    override val formErrorMsg = "Error al enviar. Comprueba tu conexión."
    override val formSendingMsg = "Enviando..."
}

/** Implementación de [AppStrings] para Inglés. */
object EnStrings : AppStrings {
    override val appName = "My Cures"
    override val disclaimerTitle = "Disclaimer"
    override val disclaimerText = """
        The "Mis Curas" application is a decision support tool aimed exclusively at nursing professionals. The suggested recommendations are based on the guidelines and consensus documents of the GNEAUPP (National Group for the Study and Advisory of Pressure Ulcers and Chronic Wounds) and the usual product catalog of the Andalusian Health Service (SAS).
        
        This application is not a substitute for clinical judgment, direct wound assessment, or comprehensive patient evaluation. The final selection of the dressing or treatment is the sole responsibility of the healthcare professional in charge, who must consider the individual characteristics of each case.
        
        The developer assumes no responsibility for clinical decisions made based on the information provided in this application, nor for the evolution, complications, or results derived from the application of the treatments suggested here.
    """.trimIndent()
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
    override val suggestProductDialogTitle = "Suggest new product"
    override val nameFieldLabel = "Your name"
    override val isHealthProLabel = "Are you a healthcare professional?"
    override val isLabLabel = "Do you belong to a laboratory?"
    override val productNameLabel = "Name of the product to suggest"
    override val productBedLabel = "Indicated wound bed"
    override val productExudateLabel = "Indicated exudate"
    override val otherSuggestionsLabel = "Other suggestions"
    override val cancelButton = "Cancel"
    override val sendButton = "Send"
    override val formSuccessMsg = "Suggestion sent successfully. Thank you!"
    override val formErrorMsg = "Error sending. Check your connection."
    override val formSendingMsg = "Sending..."
}

/** Implementación de [AppStrings] para Portugués. */
object PtStrings : AppStrings {
    override val appName = "Minhas Curas"
    override val disclaimerTitle = "Aviso de Responsabilidade"
    override val disclaimerText = """
        O aplicativo "Mis Curas" é uma ferramenta de apoio à tomada de decisão dirigida exclusivamente a profissionais de enfermagem. As recomendações sugeridas fundamentam-se nas diretrizes e documentos de consenso da GNEAUPP (Grupo Nacional para o Estudo e Assessoramento em Úlceras por Pressão e Feridas Crônicas) e no catálogo habitual de produtos do Serviço Andaluz de Saúde (SAS).
        
        Este aplicativo não substitui o julgamento clínico, a avaliação direta da ferida ou a avaliação abrangente do paciente. A seleção final do curativo ou tratamento é de responsabilidade exclusiva do profissional de saúde responsável, que deve considerar as características individuais de cada caso.
        
        O desenvolvedor não assume qualquer responsabilidade por decisões clínicas tomadas com base nas informações fornecidas neste aplicativo, nem pela evolução, complicações ou resultados derivados da aplicação dos tratamentos aqui sugeridos.
    """.trimIndent()
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
    override val suggestProductDialogTitle = "Sugerir novo produto"
    override val nameFieldLabel = "Seu nome"
    override val isHealthProLabel = "Você é profissional de saúde?"
    override val isLabLabel = "Você pertence a um laboratório?"
    override val productNameLabel = "Nome do produto a sugerir"
    override val productBedLabel = "Leito indicado"
    override val productExudateLabel = "Exsudato indicado"
    override val otherSuggestionsLabel = "Outras sugestões"
    override val cancelButton = "Cancelar"
    override val sendButton = "Enviar"
    override val formSuccessMsg = "Sugestão enviada com sucesso. Obrigado!"
    override val formErrorMsg = "Erro ao enviar. Verifique sua conexão."
    override val formSendingMsg = "Enviando..."
}
