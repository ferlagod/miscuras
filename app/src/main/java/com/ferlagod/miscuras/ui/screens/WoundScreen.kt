/*
 * Mis Curas
 * Copyright (C) 2026 Fernando Lago (ferlagod)
 *
 * Este programa es software libre: puede redistribuirlo y/o modificarlo
 * bajo los términos de la Licencia Pública General GNU publicada por
 * la Free Software Foundation, ya sea la versión 3 de la Licencia, o
 * (a su elección) cualquier versión posterior.
 */
package com.ferlagod.miscuras.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MenuBook
import com.ferlagod.miscuras.ui.AppStrings
import com.ferlagod.miscuras.ui.screens.GlossaryScreen
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ferlagod.miscuras.data.entities.ApositoEntity
import com.ferlagod.miscuras.ui.WoundUiState
import com.ferlagod.miscuras.ui.WoundViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import com.ferlagod.miscuras.R

// ============================================================
// PANTALLA PRINCIPAL — Router entre Selección y Resultados
// ============================================================

private enum class ScreenState {
    Splash, Selection, Results, Glossary, ArMeasure, Braden
}

/**
 * Pantalla principal de la aplicación.
 * Orquesta la navegación interna (Splash -> Selección -> Resultados) y gestiona
 * los diálogos de ajustes y descargo de responsabilidad.
 *
 * @param viewModel El [WoundViewModel] que provee el estado y las acciones.
 */
@Composable
/**
 * Pantalla principal del asistente de evaluación de heridas.
 * Orquesta el flujo de pasos (etiología, lecho, exudado, etc.) y muestra las recomendaciones finales.
 */
fun WoundScreen(viewModel: WoundViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    val strings = AppStrings.getStrings(uiState.currentLanguage)

    if (!uiState.showSplash && !uiState.hasSeenDisclaimer) {
        DisclaimerDialog(
            strings = strings,
            onAccept = { viewModel.acceptDisclaimer() }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentLanguage = uiState.currentLanguage,
            onLanguageChanged = { viewModel.changeLanguage(it) },
            currentTheme = uiState.currentTheme,
            onThemeChanged = { viewModel.changeTheme(it) },
            onDismiss = { showSettings = false },
            onSuggestProductClick = { 
                showSettings = false
                viewModel.setAddProductDialogVisibility(true)
            }
        )
    }

    AnimatedContent(
        targetState = when {
            uiState.showSplash -> ScreenState.Splash
            uiState.showBraden -> ScreenState.Braden
            uiState.showGlossary -> ScreenState.Glossary
            uiState.showArMeasure -> ScreenState.ArMeasure
            uiState.showResults -> ScreenState.Results
            else -> ScreenState.Selection
        },
        transitionSpec = {
            if (initialState == ScreenState.Splash) {
                // Desvanecimiento suave al salir del splash
                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
            } else if (targetState == ScreenState.Results) {
                // Entrada lateral hacia resultados
                (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
            } else {
                // Entrada lateral hacia selección
                (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
            }
        },
        label = "screen_transition"
    ) { screenState ->
        when (screenState) {
            ScreenState.Splash -> SplashContent(strings = strings)
            ScreenState.Selection -> {
                SelectionContent(
                    uiState = uiState,
                    strings = strings,
                    onEtiologyChanged = { viewModel.onEtiologyChanged(it) },
                    onLechoChanged = { viewModel.onLechoChanged(it) },
                    onExudadoChanged = { viewModel.onExudadoChanged(it) },
                    onExudateTypeChanged = { viewModel.onExudateTypeChanged(it) },
                    onBordesChanged = { viewModel.onBordesChanged(it) },
                    onPerilesionalChanged = { viewModel.onPerilesionalChanged(it) },
                    onInfeccionChanged = { viewModel.onInfeccionChanged(it) },
                    onInfectionGermChanged = { viewModel.onInfectionGermChanged(it) },
                    onPainLevelChanged = { viewModel.onPainLevelChanged(it) },
                    onWoundLengthChanged = { viewModel.onWoundLengthChanged(it) },
                    onWoundWidthChanged = { viewModel.onWoundWidthChanged(it) },
                    onWoundDepthChanged = { viewModel.onWoundDepthChanged(it) },
                    onHasCavitationChanged = { viewModel.onHasCavitationChanged(it) },
                    onCavitationDetailsChanged = { viewModel.onCavitationDetailsChanged(it) },
                    onSpecialLocationChanged = { viewModel.onSpecialLocationChanged(it) },
                    onFindApositoClick = { viewModel.buscarAposito() },
                    onNextStep = { viewModel.nextStep() },
                    onPreviousStep = { viewModel.previousStep() },
                    onSettingsClick = { showSettings = true },
                    onGlossaryClick = { viewModel.showGlossary() },
                    onArMeasureClick = { viewModel.showArMeasure() },
                    onBradenClick = { viewModel.showBraden() }
                )
            }
            ScreenState.Results -> {
                ResultsContent(
                    uiState = uiState,
                    strings = strings,
                    onBack = { viewModel.volverASeleccion() },
                    onSuggestProductClick = { viewModel.setAddProductDialogVisibility(true) },
                    onCopyProductSummary = { productoNombre -> viewModel.generarResumenEvolutivo(productoNombre, strings) }
                )
            }
            ScreenState.Glossary -> {
                GlossaryScreen(
                    onBackClick = { viewModel.hideGlossary() },
                    currentLanguage = uiState.currentLanguage
                )
            }
            ScreenState.Braden -> {
                com.ferlagod.miscuras.ui.screens.BradenScreen(
                    onBackClick = { viewModel.hideBraden() },
                    onScoreCalculated = { score -> viewModel.onBradenScoreUpdated(score) },
                    strings = strings
                )
            }
            ScreenState.ArMeasure -> {
                com.ferlagod.miscuras.ui.screens.ARMeasureScreen(
                    strings = strings,
                    onBackClick = { viewModel.hideArMeasure() },
                    onMeasured = { length, width -> viewModel.onArMeasured(length, width) }
                )
            }

        }
    }

    if (uiState.showAddProductDialog) {
        SuggestProductDialog(
            strings = strings,
            isSubmitting = uiState.isFormSubmitting,
            onDismiss = { viewModel.setAddProductDialogVisibility(false) },
            onSubmit = { name, isHealthPro, isLab, product, bed, exudate, other ->
                viewModel.submitProductSuggestion(
                    name = name,
                    isHealthProfessional = isHealthPro,
                    belongsToLaboratory = isLab,
                    productName = product,
                    woundBed = bed,
                    exudateLevel = exudate,
                    otherSuggestions = other,
                    strings = strings
                )
            }
        )
    }

    if (uiState.formResultMsg != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResultMsg() },
            title = { Text(strings.resultsTitle) },
            text = { Text(uiState.formResultMsg ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFormResultMsg() }) {
                    Text(strings.closeButton)
                }
            }
        )
    }
}

/**
 * Componente que muestra la pantalla de carga (Splash) inicial
 * con una breve animación de desvanecimiento y escala.
 *
 * @param strings Textos localizados para el título y subtítulo.
 */
@Composable
private fun SplashContent(strings: AppStrings) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1000),
        label = "scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(
                alpha = alphaAnim,
                scaleX = scaleAnim,
                scaleY = scaleAnim
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.splashSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ============================================================
// PANTALLA DE SELECCIÓN — Formulario de evaluación
// ============================================================

/**
 * Vista del formulario de evaluación clínica.
 * Permite al usuario seleccionar el estado del lecho, el nivel de exudado
 * y si hay presencia de infección.
 *
 * @param uiState Estado actual de la UI con las selecciones.
 * @param strings Textos localizados.
 * @param onLechoChanged Callback cuando se cambia el lecho.
 * @param onExudadoChanged Callback cuando se cambia el exudado.
 * @param onInfeccionChanged Callback cuando se cambia el estado de infección.
 * @param onFindApositoClick Callback para iniciar la búsqueda de apósitos.
 * @param onSettingsClick Callback para abrir el menú de ajustes.
 * @param onGlossaryClick Callback para abrir el glosario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private /**
 * Contenido del asistente paso a paso.
 * Renderiza la interfaz correspondiente al paso actual del [WizardStep].
 */
fun SelectionContent(
    uiState: WoundUiState,
    strings: AppStrings,
    onEtiologyChanged: (String) -> Unit,
    onLechoChanged: (String) -> Unit,
    onExudadoChanged: (String) -> Unit,
    onExudateTypeChanged: (String) -> Unit,
    onBordesChanged: (String) -> Unit,
    onPerilesionalChanged: (String) -> Unit,
    onInfeccionChanged: (Boolean) -> Unit,
    onInfectionGermChanged: (String) -> Unit,
    onPainLevelChanged: (Float) -> Unit,
    onWoundLengthChanged: (String) -> Unit,
    onWoundWidthChanged: (String) -> Unit,
    onWoundDepthChanged: (String) -> Unit,
    onHasCavitationChanged: (Boolean) -> Unit,
    onCavitationDetailsChanged: (String) -> Unit,
    onSpecialLocationChanged: (String) -> Unit,
    onFindApositoClick: () -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onSettingsClick: () -> Unit,
    onGlossaryClick: () -> Unit,
    onArMeasureClick: () -> Unit,
    onBradenClick: () -> Unit
) {
    // Gestor de permisos de cámara
    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onArMeasureClick()
        }
    }
    
    
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            containerColor = Color.Transparent,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(36.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.selectionHeaderTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        if (uiState.currentWizardStep != com.ferlagod.miscuras.ui.WizardStep.ETIOLOGY) {
                            IconButton(onClick = onPreviousStep) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Atrás"
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onBradenClick) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Calculadora Braden"
                            )
                        }
                        IconButton(onClick = onGlossaryClick) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Glosario / Biblioteca"
                            )
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = strings.settingsTitle
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                LinearProgressIndicator(
                    progress = { uiState.currentWizardStep.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    ) { innerPadding ->
        val isPielIntacta = uiState.selectedLecho == "Piel Intacta (Prevención)"
        AnimatedContent(
            targetState = uiState.currentWizardStep,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "wizard_animation",
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) { step ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                contentPadding = PaddingValues(vertical = 32.dp)
            ) {
                when (step) {
                    com.ferlagod.miscuras.ui.WizardStep.ETIOLOGY -> {
                        item {
                val currentEtiologyTrans = AppStrings.translateClinicalTerm(uiState.selectedEtiology, uiState.currentLanguage)
                val optionsEtiologyTrans = WoundViewModel.opcionesEtiologia.map { AppStrings.translateClinicalTerm(it, uiState.currentLanguage) }.sorted()
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = strings.welcomeTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.welcomeDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                ChipGroupCard(
                    label = strings.etiologyLabel,
                    description = strings.etiologyDesc,
                    selectedOption = currentEtiologyTrans,
                    options = optionsEtiologyTrans,
                    onOptionSelected = { 
                        onEtiologyChanged(AppStrings.mapToDbTerm(it))
                    },
                    chipType = "etiology"
                )
            }
                        item {
                val currentLechoTrans = AppStrings.translateClinicalTerm(uiState.selectedLecho, uiState.currentLanguage)
                val optionsLechoTrans = WoundViewModel.opcionesLecho.map { AppStrings.translateClinicalTerm(it, uiState.currentLanguage) }
                ChipGroupCard(
                    label = strings.bedStateLabel,
                    description = strings.bedStateDesc,
                    selectedOption = currentLechoTrans,
                    options = optionsLechoTrans,
                    onOptionSelected = { onLechoChanged(AppStrings.mapToDbTerm(it)) },
                    chipType = "tissue"
                )
            }
                        item {
                            Button(
                                onClick = onNextStep, 
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                                shape = if (isPielIntacta) RoundedCornerShape(16.dp) else androidx.compose.foundation.shape.CircleShape,
                                enabled = !uiState.isLoading,
                                colors = if (isPielIntacta) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors()
                            ) {
                                if (isPielIntacta && uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else if (isPielIntacta) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.searchButton,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                } else {
                                    Text("Siguiente")
                                }
                            }
                        }
                    }
                    com.ferlagod.miscuras.ui.WizardStep.SIZE_AND_LOCATION -> {
                        item {
                    val locationOptionsTrans = listOf(
                        strings.locationNone, 
                        strings.locationHeel, 
                        strings.locationSacrum
                    )
                    val currentLocationTrans = when (uiState.specialLocation) {
                        "Talón" -> strings.locationHeel
                        "Sacro" -> strings.locationSacrum
                        else -> strings.locationNone
                    }
                    
                    SizeInputCard(
                        lengthValue = uiState.woundLength,
                        widthValue = uiState.woundWidth,
                        depthValue = uiState.woundDepth,
                        hasCavitation = uiState.hasCavitation,
                        cavitationDetails = uiState.cavitationDetails,
                        onLengthChange = onWoundLengthChanged,
                        onWidthChange = onWoundWidthChanged,
                        onDepthChange = onWoundDepthChanged,
                        onCavitationChange = onHasCavitationChanged,
                        onCavitationDetailsChange = onCavitationDetailsChanged,
                        locationSelected = currentLocationTrans,
                        locationOptions = locationOptionsTrans,
                        onLocationChange = { transLoc ->
                            val dbLoc = when(transLoc) {
                                strings.locationHeel -> "Talón"
                                strings.locationSacrum -> "Sacro"
                                else -> "Ninguno"
                            }
                            onSpecialLocationChanged(dbLoc)
                        },
                        strings = strings,
                        onArMeasureClick = {
                            val permission = android.Manifest.permission.CAMERA
                            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (isGranted) {
                                onArMeasureClick()
                            } else {
                                cameraPermissionLauncher.launch(permission)
                            }
                        }
                    )
                }
                        item {
                            Button(
                                onClick = onNextStep, 
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {
                                Text("Siguiente")
                            }
                        }
                    }
                    com.ferlagod.miscuras.ui.WizardStep.EXUDATE -> {
                        item {
                val currentExudadoTrans = AppStrings.translateClinicalTerm(uiState.selectedExudado, uiState.currentLanguage)
                val optionsExudadoTrans = WoundViewModel.opcionesExudado.map { AppStrings.translateClinicalTerm(it, uiState.currentLanguage) }
                ChipGroupCard(
                    label = strings.exudateLevelLabel,
                    description = strings.exudateLevelDesc,
                    selectedOption = currentExudadoTrans,
                    options = optionsExudadoTrans,
                    onOptionSelected = { onExudadoChanged(AppStrings.mapToDbTerm(it)) },
                    
                    chipType = "exudate"
                )
            }
                        item {
                    val currentExuTypeTrans = AppStrings.translateClinicalTerm(uiState.selectedExudateType, uiState.currentLanguage)
                    val optionsExuTypeTrans = WoundViewModel.opcionesTipoExudado.map { AppStrings.translateClinicalTerm(it, uiState.currentLanguage) }
                    ChipGroupCard(
                        label = strings.exudateTypeLabel,
                        description = strings.exudateTypeDesc,
                        selectedOption = currentExuTypeTrans,
                        options = optionsExuTypeTrans,
                        onOptionSelected = { onExudateTypeChanged(AppStrings.mapToDbTerm(it)) },
                        enabled = uiState.selectedExudado != "Nulo",
                        chipType = "exudate"
                    )
                }
                        item {
                            Button(
                                onClick = onNextStep, 
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {
                                Text("Siguiente")
                            }
                        }
                    }
                    com.ferlagod.miscuras.ui.WizardStep.EDGES -> {
                        item {
                val currentBordes = uiState.selectedBordes
                val optionsBordes = WoundViewModel.opcionesBordes
                ChipGroupCard(
                    label = strings.edgesLabel,
                    description = strings.edgesDesc,
                    selectedOption = currentBordes,
                    options = optionsBordes,
                    onOptionSelected = { onBordesChanged(it) },
                    
                    chipType = "edge"
                )
            }
                        item {
                val currentPeriTrans = AppStrings.translateClinicalTerm(uiState.selectedPerilesional, uiState.currentLanguage)
                val optionsPeriTrans = WoundViewModel.opcionesPerilesional.map { AppStrings.translateClinicalTerm(it, uiState.currentLanguage) }
                ChipGroupCard(
                    label = strings.perilesionalLabel,
                    description = strings.perilesionalDesc,
                    selectedOption = currentPeriTrans,
                    options = optionsPeriTrans,
                    onOptionSelected = { onPerilesionalChanged(AppStrings.mapToDbTerm(it)) },
                    
                    chipType = "edge"
                )
            }
                        item {
                            Button(
                                onClick = onNextStep, 
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {
                                Text("Siguiente")
                            }
                        }
                    }
                    com.ferlagod.miscuras.ui.WizardStep.INFECTION -> {
                        item {
                InfectionCard(
                    checked = uiState.selectedInfeccion,
                    onCheckedChange = onInfeccionChanged,
                    strings = strings,
                    enabled = true
                )
            }
                        item {
                    Text(
                        text = strings.infectionDisclaimer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val germOptionsTrans = listOf(
                        strings.germNone,
                        strings.germPseudomonas,
                        strings.germMRSA,
                        strings.germCandida,
                        strings.germAcinetobacter,
                        strings.germBiofilm
                    )
                    
                    val currentGermTrans = when(uiState.infectionGerm) {
                        "Pseudomonas aeruginosa" -> strings.germPseudomonas
                        "MRSA" -> strings.germMRSA
                        "Candida albicans" -> strings.germCandida
                        "Acinetobacter" -> strings.germAcinetobacter
                        "Biofilm complejo" -> strings.germBiofilm
                        else -> strings.germNone
                    }
                    
                    GermSelectorCard(
                        germSelected = currentGermTrans,
                        germOptions = germOptionsTrans,
                        onGermChange = { transGerm ->
                            val dbGerm = when(transGerm) {
                                strings.germPseudomonas -> "Pseudomonas aeruginosa"
                                strings.germMRSA -> "MRSA"
                                strings.germCandida -> "Candida albicans"
                                strings.germAcinetobacter -> "Acinetobacter"
                                strings.germBiofilm -> "Biofilm complejo"
                                else -> "Desconocido"
                            }
                            onInfectionGermChanged(dbGerm)
                        },
                        strings = strings
                    )
                }
                        item {
                PainCard(
                    painLevel = uiState.painLevel,
                    onPainChange = onPainLevelChanged,
                    enabled = !isPielIntacta
                )
            }
                        item {
                Button(
                    onClick = {
                        onFindApositoClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.searchButton,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
                    }
                }
            }
        }
    }
}
}
// ============================================================
// COMPONENTE — Tarjeta con Dropdown selector
// ============================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private /**
 * Componente reutilizable para mostrar un grupo de opciones (chips) en formato de tarjeta.
 * Se utiliza para seleccionar parámetros como etiología, lecho, cantidad de exudado, etc.
 */
fun ChipGroupCard(
    label: String,
    description: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
    chipType: String = "default" // "tissue", "exudate", "edge", "infection"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    
                    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1f, 
                        label = "scale"
                    )
                    
                    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { if (enabled) onOptionSelected(option) },
                        interactionSource = interactionSource,
                        label = { 
                            Text(
                                text = option, 
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        shape = CircleShape,
                        enabled = enabled,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp).scale(scale)
                    )
                }
            }
        }
    }
}

// ============================================================
// COMPONENTE — Tarjeta de Infección (Switch)
// ============================================================
// COMPONENTE — Tarjeta de Dolor (TIMERS)
// ============================================================

@Composable
private fun PainCard(
    painLevel: Float,
    onPainChange: (Float) -> Unit,
    enabled: Boolean = true
) {
    val painDescription = when {
        painLevel == 0f -> "Sin Dolor"
        painLevel <= 3f -> "Leve"
        painLevel <= 7f -> "Moderado"
        else -> "Severo"
    }

    val painColor = when {
        painLevel == 0f -> MaterialTheme.colorScheme.primary
        painLevel <= 3f -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        painLevel <= 7f -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f).shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Sensibilidad / Dolor (S)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Escala EVA (0-10) al cambio de apósito",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${painLevel.toInt()}/10",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = painColor
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Slider(
                value = painLevel,
                onValueChange = { if (enabled) onPainChange(it) },
                valueRange = 0f..10f,
                steps = 9,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = painColor,
                    activeTrackColor = painColor,
                    activeTickColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Text(
                text = painDescription,
                style = MaterialTheme.typography.labelLarge,
                color = painColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InfectionCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    strings: AppStrings,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!enabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            } else if (checked) {
                if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) com.ferlagod.miscuras.ui.theme.ChipInfectionDark else com.ferlagod.miscuras.ui.theme.ChipInfection
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = if (!enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else if (checked) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.infectionLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (!enabled) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else if (checked) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = if (checked) strings.infectionDetected else strings.noInfection,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.error,
                    checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    }
}


// ============================================================
// PANTALLA DE RESULTADOS
// ============================================================

/**
 * Pantalla de resultados que muestra los productos recomendados según la evaluación.
 * Maneja tanto el estado de éxito (productos encontrados) como el estado
 * sin coincidencias (no se encontró ninguna regla aplicable).
 *
 * @param uiState Estado de la interfaz con los resultados.
 * @param strings Textos localizados.
 * @param onBack Acción para regresar a la pantalla de selección.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private /**
 * Muestra los resultados de la evaluación de la herida.
 * Incluye la puntuación de Braden y las listas de apósitos recomendados (primarios y secundarios).
 */
fun ResultsContent(
    uiState: WoundUiState,
    strings: AppStrings,
    onBack: () -> Unit,
    onSuggestProductClick: () -> Unit,
    onCopyProductSummary: (String) -> String
) {
    var selectedProduct by remember { mutableStateOf<ApositoEntity?>(null) }

    // Listas de productos agrupados por uso
    val productosPrimariosAgrupados = remember(uiState.productos) { 
        uiState.productos.filter { it.usoPrimarioSecundario.contains("Primari", ignoreCase = true) || it.usoPrimarioSecundario.contains("Ambos", ignoreCase = true) }
        .groupBy { it.familiaGenerica } 
    }
    val productosSecundariosAgrupados = remember(uiState.productos) { 
        uiState.productos.filter { it.usoPrimarioSecundario.contains("Secundari", ignoreCase = true) || it.usoPrimarioSecundario.contains("Ambos", ignoreCase = true) }
        .groupBy { it.familiaGenerica } 
    }
    
    // Definir un orden lógico (Limpiadores primero, luego antimicrobianos, luego resto)
    val ordenPrioridad = listOf(
        "Limpieza de heridas", "Plata", "Malla DACC", "Cadexómero Yodado", "Alginogel", 
        "Desbridante Enzimatico", "Alginato", "Hidrofibra", "Espuma Poliuretano", 
        "Malla Silicona", "Hidrocoloide", "Hidrogel", "Colágeno", "Superabsorbente", 
        "Carbon", "Carbón y plata", "Acidos Grasos Hiperoxigenados", "Protector Cutaneo", "Pomada"
    )
    
    val familiasPrimariasOrdenadas = remember(productosPrimariosAgrupados) {
        productosPrimariosAgrupados.keys.sortedWith(compareBy<String>(
            { familia -> val idx = ordenPrioridad.indexOf(familia); if (idx != -1) idx else 999 },
            { it }
        ))
    }
    val familiasSecundariasOrdenadas = remember(productosSecundariosAgrupados) {
        productosSecundariosAgrupados.keys.sortedWith(compareBy<String>(
            { familia -> val idx = ordenPrioridad.indexOf(familia); if (idx != -1) idx else 999 },
            { it }
        ))
    }

    var expandedFamiliesPrimary by remember(familiasPrimariasOrdenadas) { mutableStateOf(emptySet<String>()) }
    var expandedFamiliesSecondary by remember(familiasSecundariasOrdenadas) { mutableStateOf(emptySet<String>()) }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.resultsTitle,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (uiState.noMatchFound) {
            // — Sin resultados —
            NoMatchContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                lecho = uiState.selectedLecho,
                exudado = uiState.selectedExudado,
                infeccion = uiState.selectedInfeccion,
                strings = strings
            )
        } else {
            // — Con resultados —
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                // Imagen del lecho
                item {
                    val lechoImageResId = when(uiState.selectedLecho) {
                        "Necrosis" -> R.drawable.img_necrosis
                        "Esfacelo" -> R.drawable.img_esfacelo
                        "Granulación" -> R.drawable.img_granulacion
                        "Epitelización" -> R.drawable.img_epitelial
                        else -> null
                    }
                    if (lechoImageResId != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = lechoImageResId),
                                contentDescription = uiState.selectedLecho,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                }
                
                // Alertas de Seguridad
                if (uiState.safetyAlerts.isNotEmpty()) {
                    item {
                        SafetyAlertsCard(alerts = uiState.safetyAlerts)
                    }
                }

                // Alerta Preventiva Braden
                if (uiState.bradenScore != null && uiState.bradenScore!! < 12) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Alerta",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = String.format(java.util.Locale.US, strings.bradenPreventiveAlert, uiState.bradenScore.toString()),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Paciente con Riesgo Alto/Muy Alto. Recomendar proactivamente Ácidos Grasos Hiperoxigenados (AGHO), espumas de poliuretano sacras/talonares de cinco capas, y colchón de aire alternante (SEMP).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // AI Response
                item {
                    AiResponseCard(
                        isLoading = uiState.isAiLoading,
                        response = uiState.aiResponse,
                        strings = strings
                    )
                }

                // Resumen de la evaluación
                item {
                    EvaluationSummaryCard(
                        lecho = uiState.selectedLecho,
                        exudado = uiState.selectedExudado,
                        infeccion = uiState.selectedInfeccion,
                        strings = strings,
                        lang = uiState.currentLanguage
                    )
                }

                // Familia recomendada
                item {
                    RecommendedFamilyCard(
                        familia = uiState.familiaRecomendada ?: "",
                        strings = strings
                    )
                }

                // Título de la sección de productos
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.primaryDressingCategory,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                familiasPrimariasOrdenadas.forEach { familia ->
                    val isExpanded = expandedFamiliesPrimary.contains(familia)
                    val productosDeFamilia = productosPrimariosAgrupados[familia] ?: emptyList()

                    item {
                        androidx.compose.material3.Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    expandedFamiliesPrimary = if (isExpanded) {
                                        expandedFamiliesPrimary - familia
                                    } else {
                                        expandedFamiliesPrimary + familia
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$familia (${productosDeFamilia.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                androidx.compose.material3.Icon(
                                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Expandir/Contraer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (isExpanded) {
                        items(productosDeFamilia) { producto ->
                            ProductCard(
                                producto = producto,
                                strings = strings,
                                onClick = {
                                    selectedProduct = producto
                                },
                                onCopyClick = {
                                    val resumen = onCopyProductSummary(producto.nombreComercial)
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(resumen))
                                    android.widget.Toast.makeText(context, strings.copySummaryToast, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = strings.secondaryDressingCategory,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                familiasSecundariasOrdenadas.forEach { familia ->
                    val isExpanded = expandedFamiliesSecondary.contains(familia)
                    val productosDeFamilia = productosSecundariosAgrupados[familia] ?: emptyList()

                    item {
                        androidx.compose.material3.Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    expandedFamiliesSecondary = if (isExpanded) {
                                        expandedFamiliesSecondary - familia
                                    } else {
                                        expandedFamiliesSecondary + familia
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$familia (${productosDeFamilia.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                androidx.compose.material3.Icon(
                                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Expandir/Contraer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    if (isExpanded) {
                        items(productosDeFamilia) { producto ->
                            ProductCard(
                                producto = producto,
                                strings = strings,
                                onClick = {
                                    selectedProduct = producto
                                },
                                onCopyClick = {
                                    val resumen = onCopyProductSummary(producto.nombreComercial)
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(resumen))
                                    android.widget.Toast.makeText(context, strings.copySummaryToast, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // Botón para sugerir producto
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onSuggestProductClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text(strings.suggestProductButton)
                    }
                }
            }
        }
    }

    // Diálogo de detalles del producto
    if (selectedProduct != null) {
        ProductDetailDialog(
            producto = selectedProduct!!,
            strings = strings,
            onDismiss = { selectedProduct = null }
        )
    }
}

// ============================================================
// COMPONENTE — Resumen de la evaluación
// ============================================================

@Composable
private fun EvaluationSummaryCard(
    lecho: String,
    exudado: String,
    infeccion: Boolean,
    strings: AppStrings,
    lang: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = strings.evaluationDone,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryChip(label = AppStrings.translateClinicalTerm(lecho, lang))
                SummaryChip(label = AppStrings.translateClinicalTerm(exudado, lang))
                if (infeccion) {
                    SummaryChip(
                        label = strings.infectionChip,
                        isError = true
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, isError: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        tonalElevation = 0.dp
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// ============================================================
// COMPONENTE — Tarjeta de familia recomendada
// ============================================================

@Composable
private fun RecommendedFamilyCard(familia: String, strings: AppStrings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (familia.contains(" y ") || familia.contains(" and ") || familia.contains(" e ")) strings.recommendedFamilyPlural else strings.recommendedFamilySingular,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = familia,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// ============================================================
// COMPONENTE — Tarjeta de producto
// ============================================================

@Composable
private fun ProductCard(
    producto: ApositoEntity,
    strings: AppStrings,
    onClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Miniatura del producto cargada con Coil
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp
            ) {
                val context = LocalContext.current
                val imageResId: Int = remember(producto.imagenUrl) {
                    val id = context.resources.getIdentifier(producto.imagenUrl, "drawable", context.packageName)
                    if (id != 0) id else R.drawable.logo
                }
                AsyncImage(
                    model = imageResId,
                    contentDescription = producto.nombreComercial,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.logo),
                    error = painterResource(id = R.drawable.logo)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombreComercial,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = producto.fabricante,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Dimensiones debajo del nombre para evitar comprimir el texto
                Spacer(modifier = Modifier.height(6.dp))
                val dimensionesList = producto.dimensiones.split("/").map { it.trim() }
                if (dimensionesList.size > 1) {
                    // Múltiples medidas: mostrar cada una en su propia línea
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        dimensionesList.forEach { dim ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = dim,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Una sola medida: badge inline
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = producto.dimensiones,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onCopyClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.copySummaryButton)
            }
        }
    }
}

/**
 * Diálogo flotante que muestra la información detallada de un producto (apósito).
 * Incluye imagen ampliada, código CN, medidas, indicaciones e interacciones.
 *
 * @param producto Entidad de la base de datos con los detalles del producto.
 * @param strings Textos localizados.
 * @param onDismiss Acción a ejecutar al cerrar el diálogo.
 */
@Composable
private fun ProductDetailDialog(
    producto: ApositoEntity,
    strings: AppStrings,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Imagen de gran tamaño con Coil
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val context = LocalContext.current
                    val imageResId: Int = remember(producto.imagenUrl) {
                        val id = context.resources.getIdentifier(producto.imagenUrl, "drawable", context.packageName)
                        if (id != 0) id else R.drawable.logo
                    }
                    AsyncImage(
                        model = imageResId,
                        contentDescription = producto.nombreComercial,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        placeholder = painterResource(id = R.drawable.logo),
                        error = painterResource(id = R.drawable.logo)
                    )
                }

                // Cabecera del producto
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = producto.nombreComercial,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (strings.languageLabel == "Idioma") "Fabricante: ${producto.fabricante}" 
                               else if (strings.languageLabel == "Language") "Manufacturer: ${producto.fabricante}"
                               else "Fabricante: ${producto.fabricante}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badges clínicos
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "CN ${producto.codigoCn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = producto.dimensiones,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        val usageTrans = when (producto.usoPrimarioSecundario) {
                            "Primario" -> strings.primaryUseLabel
                            "Secundario" -> strings.secondaryUseLabel
                            else -> "${strings.primaryUseLabel} / ${strings.secondaryUseLabel}"
                        }
                        Text(
                            text = usageTrans,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Descripción
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = strings.clinicalMechanism,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Interacciones
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = strings.precautionsTitle,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Text(
                        text = producto.interacciones,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                // Botón Cerrar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = strings.closeButton,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ============================================================
// COMPONENTE — Sin coincidencia
// ============================================================

@Composable
private fun NoMatchContent(
    modifier: Modifier = Modifier,
    lecho: String,
    exudado: String,
    infeccion: Boolean,
    strings: AppStrings
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = strings.noMatchTitle,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = strings.noMatchSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val lang = if (strings.languageLabel == "Idioma") "es" else if (strings.languageLabel == "Language") "en" else "pt"
            SummaryChip(label = AppStrings.translateClinicalTerm(lecho, lang))
            SummaryChip(label = AppStrings.translateClinicalTerm(exudado, lang))
            if (infeccion) {
                SummaryChip(label = strings.infectionChip, isError = true)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = strings.noMatchSubtitle2,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Diálogo de descargo de responsabilidad legal.
 * Debe ser aceptado por el usuario la primera vez que ingresa a la aplicación.
 *
 * @param strings Textos localizados.
 * @param onAccept Acción a ejecutar cuando el usuario acepta el descargo.
 */
@Composable
private fun DisclaimerDialog(strings: AppStrings, onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* No dismiss by clicking outside */ },
        confirmButton = {
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = strings.acceptButton,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = strings.disclaimerTitle,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.disclaimerText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

/**
 * Menú flotante (Dialog) para configurar los ajustes de la aplicación.
 * Permite cambiar el tema visual (Claro, Oscuro, Sistema) y el idioma, además de
 * mostrar información de la versión y enlaces del desarrollador.
 *
 * @param currentLanguage Idioma actual seleccionado.
 * @param onLanguageChanged Función para actualizar el idioma.
 * @param currentTheme Tema visual actual.
 * @param onThemeChanged Función para actualizar el tema.
 * @param onDismiss Acción al cerrar el diálogo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDialog(
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit,
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSuggestProductClick: () -> Unit
) {
    val strings = AppStrings.getStrings(currentLanguage)
    val uriHandler = LocalUriHandler.current
    var showDeveloperInfo by remember { mutableStateOf(false) }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // --- TEMA ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.themeLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("light" to strings.themeLight, "dark" to strings.themeDark, "system" to strings.themeSystem).forEach { (mode, label) ->
                            val selected = currentTheme == mode
                            FilterChip(
                                selected = selected,
                                onClick = { onThemeChanged(mode) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // --- IDIOMA ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.languageLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("es" to strings.languageEs, "en" to strings.languageEn, "pt" to strings.languagePt).forEach { (lang, label) ->
                            val selected = currentLanguage == lang
                            FilterChip(
                                selected = selected,
                                onClick = { onLanguageChanged(lang) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // --- SUGERENCIAS ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.settingsSuggestTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = strings.settingsSuggestDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onSuggestProductClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.suggestProductButton)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // --- INFORMACIÓN ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.appVersionLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "1.0.0",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Desarrollador",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Button(
                        onClick = { showDeveloperInfo = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.developerLabel)
                    }

                    if (showDeveloperInfo) {
                        DeveloperInfoDialog(strings = strings, onDismiss = { showDeveloperInfo = false })
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = strings.donationsLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Button(
                        onClick = { uriHandler.openUri("https://liberapay.com/ferlagod./") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.donationButtonText)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = strings.sourceCodeLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    OutlinedButton(
                        onClick = { uriHandler.openUri("https://github.com/ferlagod") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("GitHub")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Política de Privacidad",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    OutlinedButton(
                        onClick = { uriHandler.openUri("https://ferlagod.github.io/miscuras/") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ver Política de Privacidad")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = strings.closeButton,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
}

// ============================================================
// COMPONENTE — Diálogo para Sugerir Producto
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestProductDialog(
    strings: AppStrings,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, Boolean, Boolean, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isHealthPro by remember { mutableStateOf(false) }
    var isLab by remember { mutableStateOf(false) }
    var productName by remember { mutableStateOf("") }
    var woundBed by remember { mutableStateOf("") }
    var exudateLevel by remember { mutableStateOf("") }
    var otherSuggestions by remember { mutableStateOf("") }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = strings.suggestProductDialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.nameFieldLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = isHealthPro, onCheckedChange = { isHealthPro = it })
                    Text(strings.isHealthProLabel, style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = isLab, onCheckedChange = { isLab = it })
                    Text(strings.isLabLabel, style = MaterialTheme.typography.bodyMedium)
                }

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text(strings.productNameLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = woundBed,
                    onValueChange = { woundBed = it },
                    label = { Text(strings.productBedLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = exudateLevel,
                    onValueChange = { exudateLevel = it },
                    label = { Text(strings.productExudateLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = otherSuggestions,
                    onValueChange = { otherSuggestions = it },
                    label = { Text(strings.otherSuggestionsLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting
                    ) {
                        Text(strings.cancelButton)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSubmit(name, isHealthPro, isLab, productName, woundBed, exudateLevel, otherSuggestions)
                        },
                        enabled = !isSubmitting && name.isNotBlank() && productName.isNotBlank()
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.formSendingMsg)
                        } else {
                            Text(strings.sendButton)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
}

// ============================================================
// COMPONENTE — Tarjeta de Tamaño y Ubicación
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeInputCard(
    lengthValue: String,
    widthValue: String,
    depthValue: String,
    hasCavitation: Boolean,
    cavitationDetails: String,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onDepthChange: (String) -> Unit,
    onCavitationChange: (Boolean) -> Unit,
    onCavitationDetailsChange: (String) -> Unit,
    locationSelected: String,
    locationOptions: List<String>,
    onLocationChange: (String) -> Unit,
    strings: AppStrings,
    onArMeasureClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth().shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.woundSizeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.woundSizeDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = lengthValue,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*[,.]?\\d*\$"))) onLengthChange(it) 
                    },
                    label = { Text(strings.woundLengthLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = widthValue,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*[,.]?\\d*\$"))) onWidthChange(it) 
                    },
                    label = { Text(strings.woundWidthLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = depthValue,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*[,.]?\\d*\$"))) onDepthChange(it) 
                    },
                    label = { Text(strings.woundDepthLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                IconButton(
                    onClick = onArMeasureClick,
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CameraAlt,
                        contentDescription = "Measure with AR",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onCavitationChange(!hasCavitation) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.cavitationLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.cavitationDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = hasCavitation,
                    onCheckedChange = onCavitationChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            androidx.compose.animation.AnimatedVisibility(visible = hasCavitation) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cavitationDetails,
                        onValueChange = onCavitationDetailsChange,
                        label = { Text(strings.cavitationDetailsLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = strings.specialLocationLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = strings.specialLocationDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = locationSelected,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    locationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontWeight = if (option == locationSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onLocationChange(option)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            trailingIcon = {
                                if (option == locationSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// COMPONENTE — Tarjeta de Selección de Germen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GermSelectorCard(
    germSelected: String,
    germOptions: List<String>,
    onGermChange: (String) -> Unit,
    strings: AppStrings
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = strings.germLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = strings.germDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = germSelected,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        unfocusedBorderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    germOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontWeight = if (option == germSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onGermChange(option)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            trailingIcon = {
                                if (option == germSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// COMPONENTE — AI Response Card
// ============================================================
@Composable
private fun SafetyAlertsCard(alerts: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                    contentDescription = "Alerta Clínica",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alertas de Seguridad Clínica",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            alerts.forEach { alert ->
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = alert,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun AiResponseCard(isLoading: Boolean, response: String?, strings: AppStrings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.aiAssistantTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = parseSimpleMarkdown(response ?: strings.aiResponseError),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun parseSimpleMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    val regex = Regex("\\*\\*(.*?)\\*\\*|\\*(.*?)\\*")
    var lastIndex = 0
    regex.findAll(text).forEach { matchResult ->
        builder.append(text.substring(lastIndex, matchResult.range.first))
        if (matchResult.groups[1] != null) {
            builder.withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                append(matchResult.groups[1]!!.value)
            }
        } else if (matchResult.groups[2] != null) {
            // Check if it's not a bullet point at the start of a line
            val inner = matchResult.groups[2]!!.value
            if (inner.trim().isEmpty() || inner.contains("\n")) {
                builder.append(matchResult.value)
            } else {
                builder.withStyle(androidx.compose.ui.text.SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) {
                    append(inner)
                }
            }
        }
        lastIndex = matchResult.range.last + 1
    }
    builder.append(text.substring(lastIndex, text.length))
    return builder.toAnnotatedString()
}

@Composable
fun DeveloperInfoDialog(strings: AppStrings, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(strings.closeButton)
            }
        },
        title = { Text(strings.developerLabel) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(120.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = com.ferlagod.miscuras.R.drawable.perfil_rrss),
                        contentDescription = "Foto de perfil de ferlagod",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Text(
                    text = "ferlagod (Fernando Lago) | Enfermero y desarrollador de software libre.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Apasionado por la tecnología independiente, la privacidad y la lectura. Desarrollo herramientas nacidas de mis propias aficiones y necesidades profesionales, como BiblioHouse, Mis Curas y Rocinante.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}