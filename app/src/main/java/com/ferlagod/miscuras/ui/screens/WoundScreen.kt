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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AddCircleOutline
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
import com.ferlagod.miscuras.ui.ClinicalTermMapper
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
import com.ferlagod.miscuras.ui.WizardState
import com.ferlagod.miscuras.ui.EvaluationState
import com.ferlagod.miscuras.ui.ConfigState
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
import androidx.compose.ui.window.DialogProperties
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.FileOutputStream
import java.io.InputStream
import com.ferlagod.miscuras.R
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
 * Pantalla principal del asistente de evaluación de heridas.
 * Orquesta el flujo de pasos (etiología, lecho, exudado, etc.) y muestra las recomendaciones finales.
 */
@Composable
fun WoundScreen(
    viewModel: WoundViewModel,
    woundIdForSave: Long? = null,
    onBackToDashboard: () -> Unit = {}
) {
    val wizardState by viewModel.wizardState.collectAsStateWithLifecycle()
    val evaluationState by viewModel.evaluationState.collectAsStateWithLifecycle()
    val configState by viewModel.configState.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }
    val context = LocalContext.current
        if (!configState.showSplash && !configState.hasSeenDisclaimer) {
        DisclaimerDialog(
            onAccept = { viewModel.acceptDisclaimer() }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentLanguage = configState.currentLanguage,
            onLanguageChanged = { viewModel.changeLanguage(it) },
            currentTheme = configState.currentTheme,
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
            configState.showSplash -> ScreenState.Splash
            configState.showBraden -> ScreenState.Braden
            configState.showGlossary -> ScreenState.Glossary
            configState.showArMeasure -> ScreenState.ArMeasure
            evaluationState.showResults -> ScreenState.Results
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
            ScreenState.Splash -> SplashContent()
            ScreenState.Selection -> {
                SelectionContent(
                    wizardState = wizardState, evaluationState = evaluationState, configState = configState,
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
                    wizardState = wizardState, evaluationState = evaluationState, configState = configState,
                    onPreviousStep = { viewModel.previousStep() },
                    onStartOver = { 
                        viewModel.resetWizard()
                        onBackToDashboard()
                    },
                    onSaveEvaluation = if (woundIdForSave != null) {
                        { viewModel.saveEvaluation(woundIdForSave) }
                    } else null,
                    onSuggestProductClick = { viewModel.setAddProductDialogVisibility(true) },
                    onCopyProductSummary = { productoNombre -> viewModel.generarResumenEvolutivo(productoNombre, context) },
                    onToggleProductSelection = { codigoCn -> viewModel.toggleProductSelection(codigoCn) },
                    onPhotoPathChanged = { viewModel.setPhotoPath(it) },
                    context = context
                )
            }
            ScreenState.Glossary -> {
                GlossaryScreen(
                    onBackClick = { viewModel.hideGlossary() },
                    currentLanguage = configState.currentLanguage
                )
            }
            ScreenState.Braden -> {
                com.ferlagod.miscuras.ui.screens.BradenScreen(
                    onBackClick = { viewModel.hideBraden() },
                    onScoreCalculated = { score -> viewModel.onBradenScoreUpdated(score) },
                    )
            }
            ScreenState.ArMeasure -> {
                com.ferlagod.miscuras.ui.screens.ARMeasureScreen(
                    onBackClick = { viewModel.hideArMeasure() },
                    onMeasured = { length, width -> viewModel.onArMeasured(length, width) }
                )
            }

        }
    }

    if (configState.showAddProductDialog) {
        SuggestProductDialog(
            isSubmitting = configState.isFormSubmitting,
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
                    context = context
                    )
            }
        )
    }

    if (configState.formResultMsg != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearFormResultMsg() },
            title = { Text(stringResource(R.string.results_title)) },
            text = { Text(configState.formResultMsg ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFormResultMsg() }) {
                    Text(stringResource(R.string.close_button))
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
private fun SplashContent() {
    val context = LocalContext.current
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
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_subtitle),
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
private fun SelectionContent(
    wizardState: WizardState, evaluationState: EvaluationState, configState: ConfigState,
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
    val coroutineScope = rememberCoroutineScope()
    // Gestor de permisos de cámara
    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onArMeasureClick()
        }
    }
    
    
    val context = androidx.compose.ui.platform.LocalContext.current

    BackHandler(enabled = wizardState.currentWizardStep != com.ferlagod.miscuras.ui.WizardStep.ETIOLOGY) {
        onPreviousStep()
    }

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
                                text = stringResource(R.string.selection_header_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        if (wizardState.currentWizardStep != com.ferlagod.miscuras.ui.WizardStep.ETIOLOGY) {
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
                                contentDescription = stringResource(R.string.settings_title)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                LinearProgressIndicator(
                    progress = { wizardState.currentWizardStep.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    ) { innerPadding ->
        val isPielIntacta = wizardState.selectedLecho == "Piel Intacta (Prevención)"
        AnimatedContent(
            targetState = wizardState.currentWizardStep,
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
                val currentEtiologyTrans = ClinicalTermMapper.translateClinicalTerm(wizardState.selectedEtiology, context)
                val optionsEtiologyTrans = WoundViewModel.opcionesEtiologia.map { ClinicalTermMapper.translateClinicalTerm(it, context) }.sorted()
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.welcome_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.welcome_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                ChipGroupCard(
                    label = stringResource(R.string.etiology_label),
                    description = stringResource(R.string.etiology_desc),
                    selectedOption = currentEtiologyTrans,
                    options = optionsEtiologyTrans,
                    onOptionSelected = { 
                        onEtiologyChanged(ClinicalTermMapper.mapToDbTerm(it, context))
                    },
                    chipType = "etiology"
                )
            }
                        item {
                val currentLechoTrans = ClinicalTermMapper.translateClinicalTerm(wizardState.selectedLecho, context)
                val optionsLechoTrans = WoundViewModel.opcionesLecho.map { ClinicalTermMapper.translateClinicalTerm(it, context) }
                ChipGroupCard(
                    label = stringResource(R.string.bed_state_label),
                    description = stringResource(R.string.bed_state_desc),
                    selectedOption = currentLechoTrans,
                    options = optionsLechoTrans,
                    onOptionSelected = { 
                        onLechoChanged(ClinicalTermMapper.mapToDbTerm(it, context)) 
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(600)
                            onNextStep()
                        }
                    },
                    chipType = "tissue"
                )
            }
                        if (isPielIntacta) {
                            item {
                                Button(
                                    onClick = onNextStep, 
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = !evaluationState.isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    if (evaluationState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Analizando prevención...",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.search_button),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                    com.ferlagod.miscuras.ui.WizardStep.SIZE_AND_LOCATION -> {
                        item {
                    val locationOptionsTrans = listOf(
                        stringResource(R.string.location_none), 
                        context.getString(R.string.location_heel), 
                        context.getString(R.string.location_sacrum)
                    )
                    val currentLocationTrans = when (wizardState.specialLocation) {
                        "Talón" -> context.getString(R.string.location_heel)
                        "Sacro" -> context.getString(R.string.location_sacrum)
                        else -> stringResource(R.string.location_none)
                    }
                    
                    SizeInputCard(
                        lengthValue = wizardState.woundLength,
                        widthValue = wizardState.woundWidth,
                        depthValue = wizardState.woundDepth,
                        hasCavitation = wizardState.hasCavitation,
                        cavitationDetails = wizardState.cavitationDetails,
                        onLengthChange = onWoundLengthChanged,
                        onWidthChange = onWoundWidthChanged,
                        onDepthChange = onWoundDepthChanged,
                        onCavitationChange = onHasCavitationChanged,
                        onCavitationDetailsChange = onCavitationDetailsChanged,
                        locationSelected = currentLocationTrans,
                        locationOptions = locationOptionsTrans,
                        onLocationChange = { transLoc ->
                            val dbLoc = when(transLoc) {
                                context.getString(R.string.location_heel) -> "Talón"
                                context.getString(R.string.location_sacrum) -> "Sacro"
                                else -> "Ninguno"
                            }
                            onSpecialLocationChanged(dbLoc)
                        },
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
                val currentExudadoTrans = ClinicalTermMapper.translateClinicalTerm(wizardState.selectedExudado, context)
                val optionsExudadoTrans = WoundViewModel.opcionesExudado.map { ClinicalTermMapper.translateClinicalTerm(it, context) }
                ChipGroupCard(
                    label = stringResource(R.string.exudate_level_label),
                    description = stringResource(R.string.exudate_level_desc),
                    selectedOption = currentExudadoTrans,
                    options = optionsExudadoTrans,
                    onOptionSelected = { 
                        val dbTerm = ClinicalTermMapper.mapToDbTerm(it, context)
                        onExudadoChanged(dbTerm) 
                        if (dbTerm == "Nulo") {
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(600)
                                onNextStep()
                            }
                        }
                    },
                    
                    chipType = "exudate"
                )
            }
                        item {
                    val currentExuTypeTrans = ClinicalTermMapper.translateClinicalTerm(wizardState.selectedExudateType, context)
                    val optionsExuTypeTrans = WoundViewModel.opcionesTipoExudado.map { ClinicalTermMapper.translateClinicalTerm(it, context) }
                    ChipGroupCard(
                        label = stringResource(R.string.exudate_type_label),
                        description = stringResource(R.string.exudate_type_desc),
                        selectedOption = currentExuTypeTrans,
                        options = optionsExuTypeTrans,
                        onOptionSelected = { 
                            onExudateTypeChanged(ClinicalTermMapper.mapToDbTerm(it, context)) 
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(600)
                                onNextStep()
                            }
                        },
                        enabled = wizardState.selectedExudado != "Nulo",
                        chipType = "exudate"
                    )
                }
                    }
                    com.ferlagod.miscuras.ui.WizardStep.EDGES -> {
                        item {
                val currentBordes = wizardState.selectedBordes
                val optionsBordes = WoundViewModel.opcionesBordes
                ChipGroupCard(
                    label = stringResource(R.string.edges_label),
                    description = stringResource(R.string.edges_desc),
                    selectedOption = currentBordes,
                    options = optionsBordes,
                    onOptionSelected = { 
                        onBordesChanged(it) 
                    },
                    
                    chipType = "edge"
                )
            }
                        item {
                val currentPeriTrans = ClinicalTermMapper.translateClinicalTerm(wizardState.selectedPerilesional, context)
                val optionsPeriTrans = WoundViewModel.opcionesPerilesional.map { ClinicalTermMapper.translateClinicalTerm(it, context) }
                ChipGroupCard(
                    label = stringResource(R.string.perilesional_label),
                    description = stringResource(R.string.perilesional_desc),
                    selectedOption = currentPeriTrans,
                    options = optionsPeriTrans,
                    onOptionSelected = { 
                        onPerilesionalChanged(ClinicalTermMapper.mapToDbTerm(it, context)) 
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(600)
                            onNextStep()
                        }
                    },
                    
                    chipType = "edge"
                )
            }
                    }
                    com.ferlagod.miscuras.ui.WizardStep.INFECTION -> {
                        item {
                InfectionCard(
                    checked = wizardState.selectedInfeccion,
                    onCheckedChange = onInfeccionChanged,
                    enabled = true
                )
            }
                        item {
                    Text(
                        text = stringResource(R.string.infection_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val germOptionsTrans = listOf(
                        stringResource(R.string.germ_none),
                        context.getString(R.string.germ_pseudomonas),
                        context.getString(R.string.germ_mrsa),
                        context.getString(R.string.germ_candida),
                        context.getString(R.string.germ_acinetobacter),
                        context.getString(R.string.germ_biofilm)
                    )
                    
                    val currentGermTrans = when(wizardState.infectionGerm) {
                        "Pseudomonas aeruginosa" -> context.getString(R.string.germ_pseudomonas)
                        "MRSA" -> context.getString(R.string.germ_mrsa)
                        "Candida albicans" -> context.getString(R.string.germ_candida)
                        "Acinetobacter" -> context.getString(R.string.germ_acinetobacter)
                        "Biofilm complejo" -> context.getString(R.string.germ_biofilm)
                        else -> stringResource(R.string.germ_none)
                    }
                    
                    GermSelectorCard(
                        germSelected = currentGermTrans,
                        germOptions = germOptionsTrans,
                        onGermChange = { transGerm ->
                            val dbGerm = when(transGerm) {
                                context.getString(R.string.germ_pseudomonas) -> "Pseudomonas aeruginosa"
                                context.getString(R.string.germ_mrsa) -> "MRSA"
                                context.getString(R.string.germ_candida) -> "Candida albicans"
                                context.getString(R.string.germ_acinetobacter) -> "Acinetobacter"
                                context.getString(R.string.germ_biofilm) -> "Biofilm complejo"
                                else -> "Desconocido"
                            }
                            onInfectionGermChanged(dbGerm)
                        },
                        )
                }
                        item {
                PainCard(
                    painLevel = wizardState.painLevel,
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
                    enabled = !evaluationState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (evaluationState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Analizando lecho y exudado...",
                            style = MaterialTheme.typography.labelLarge
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.search_button),
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
private fun ChipGroupCard(
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
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp).scale(scale)
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
                    text = stringResource(R.string.infection_label),
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
                    text = if (checked) stringResource(R.string.infection_detected) else stringResource(R.string.no_infection),
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
private fun ResultsContent(
    wizardState: WizardState, evaluationState: EvaluationState, configState: ConfigState,
    onPreviousStep: () -> Unit,
    onStartOver: () -> Unit,
    onSaveEvaluation: (() -> Unit)?,
    onSuggestProductClick: () -> Unit,
    onCopyProductSummary: (String) -> String,
    onToggleProductSelection: (String) -> Unit,
    onPhotoPathChanged: (String) -> Unit,
    context: android.content.Context
) {
    var selectedProduct by remember { mutableStateOf<ApositoEntity?>(null) }

    // Helpers para fotos
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoFile != null) {
            onPhotoPathChanged(currentPhotoFile!!.absolutePath)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.filesDir, "photos")
            if (!photoFile.exists()) photoFile.mkdirs()
            val newFile = File(photoFile, "IMG_CAM_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())}.jpg")
            currentPhotoFile = newFile
            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)
            takePictureLauncher.launch(uri)
        } else {
            android.widget.Toast.makeText(context, "Permiso de cámara denegado", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Copiar la imagen a nuestro almacenamiento interno
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val photoFile = File(context.filesDir, "photos")
                if (!photoFile.exists()) photoFile.mkdirs()
                val newFile = File(photoFile, "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg")
                val outputStream = FileOutputStream(newFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                // Propagamos al ViewModel
                onPhotoPathChanged(newFile.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Listas de productos agrupados por uso
    val productosPrimariosAgrupados = remember(evaluationState.productos) { 
        evaluationState.productos.filter { it.usoPrimarioSecundario.contains("Primari", ignoreCase = true) || it.usoPrimarioSecundario.contains("Ambos", ignoreCase = true) }
        .groupBy { it.familiaGenerica } 
    }
    val productosSecundariosAgrupados = remember(evaluationState.productos) { 
        evaluationState.productos.filter { it.usoPrimarioSecundario.contains("Secundari", ignoreCase = true) || it.usoPrimarioSecundario.contains("Ambos", ignoreCase = true) }
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
                        text = stringResource(R.string.results_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onPreviousStep) {
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
        if (evaluationState.noMatchFound) {
            // — Sin resultados —
            NoMatchContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                lecho = wizardState.selectedLecho,
                exudado = wizardState.selectedExudado,
                infeccion = wizardState.selectedInfeccion,
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
                    val lechoImageResId = when(wizardState.selectedLecho) {
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
                                contentDescription = wizardState.selectedLecho,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                }
                
                // Alertas de Seguridad
                if (evaluationState.safetyAlerts.isNotEmpty()) {
                    item {
                        SafetyAlertsCard(alerts = evaluationState.safetyAlerts)
                    }
                }

                // Alerta Preventiva Braden
                if (evaluationState.bradenScore != null && evaluationState.bradenScore!! < 12) {
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
                                        text = String.format(java.util.Locale.US, stringResource(R.string.braden_preventive_alert), evaluationState.bradenScore.toString()),
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
                        isLoading = evaluationState.isAiLoading,
                        response = evaluationState.aiResponse,
                        )
                }

                // Resumen de la evaluación
                item {
                    EvaluationSummaryCard(
                        lecho = wizardState.selectedLecho,
                        exudado = wizardState.selectedExudado,
                        infeccion = wizardState.selectedInfeccion,
                        lang = configState.currentLanguage
                    )
                }

                // Familia recomendada
                item {
                    RecommendedFamilyCard(
                        familia = evaluationState.familiaRecomendada ?: "",
                        )
                }

                // Frecuencia de Cura
                if (evaluationState.cureFrequency != null) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        FrequencyCard(
                            frequency = evaluationState.cureFrequency
                        )
                    }
                }

                // Título de la sección de productos
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.primary_dressing_category),
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
                                isSelected = evaluationState.selectedTreatmentProducts.contains(producto.nombreComercial),
                                onClick = {
                                    selectedProduct = producto
                                },
                                onCopyClick = {
                                    val resumen = onCopyProductSummary(producto.nombreComercial)
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(resumen))
                                    android.widget.Toast.makeText(context, context.getString(R.string.copy_summary_toast), android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onSelectClick = {
                                    onToggleProductSelection(producto.nombreComercial)
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.secondary_dressing_category),
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
                                isSelected = evaluationState.selectedTreatmentProducts.contains(producto.nombreComercial),
                                onClick = {
                                    selectedProduct = producto
                                },
                                onCopyClick = {
                                    val resumen = onCopyProductSummary(producto.nombreComercial)
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(resumen))
                                    android.widget.Toast.makeText(context, context.getString(R.string.copy_summary_toast), android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onSelectClick = {
                                    onToggleProductSelection(producto.nombreComercial)
                                }
                            )
                        }
                    }
                }

                // Sección de Foto (Opcional)
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Registro Fotográfico (Opcional)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (wizardState.photoPath != null) {
                                AsyncImage(
                                    model = File(wizardState.photoPath),
                                    contentDescription = "Foto de la herida",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { onPhotoPathChanged("") }) { // TODO: borrar uri
                                    Text("Eliminar foto", color = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = {
                                            val permission = android.Manifest.permission.CAMERA
                                            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                            if (isGranted) {
                                                // Create temp file for camera
                                                val photoFile = File(context.filesDir, "photos")
                                                if (!photoFile.exists()) photoFile.mkdirs()
                                                val newFile = File(photoFile, "IMG_CAM_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())}.jpg")
                                                currentPhotoFile = newFile
                                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)
                                                takePictureLauncher.launch(uri)
                                            } else {
                                                cameraPermissionLauncher.launch(permission)
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Tomar Foto")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = {
                                            pickMediaLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        }
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Galería")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Galería")
                                    }
                                }
                            }
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
                        Text(stringResource(R.string.suggest_product_button))
                    }
                }
                
                // Botón de Guardado Final
                if (onSaveEvaluation != null) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onSaveEvaluation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.Save, contentDescription = "Guardar")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Guardar Evaluación",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de detalles del producto
    if (selectedProduct != null) {
        ProductDetailDialog(
            producto = selectedProduct!!,
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
    lang: String
) {
    val context = LocalContext.current
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
                text = stringResource(R.string.evaluation_done),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryChip(label = ClinicalTermMapper.translateClinicalTerm(lecho, context))
                SummaryChip(label = ClinicalTermMapper.translateClinicalTerm(exudado, context))
                if (infeccion) {
                    SummaryChip(
                        label = stringResource(R.string.infection_chip),
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
private fun RecommendedFamilyCard(familia: String, ) {
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
                    text = if (familia.contains(" y ") || familia.contains(" and ") || familia.contains(" e ")) stringResource(R.string.recommended_family_plural) else stringResource(R.string.recommended_family_singular),
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

@Composable
private fun FrequencyCard(frequency: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                color = MaterialTheme.colorScheme.tertiary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Pauta de Cura Recomendada",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = frequency,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
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
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onCopyClick: () -> Unit,
    onSelectClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onSelectClick != null) {
                    Button(
                        onClick = onSelectClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (isSelected) androidx.compose.material.icons.Icons.Default.CheckCircle else androidx.compose.material.icons.Icons.Outlined.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSelected) "Añadido" else "Añadir")
                    }
                }

                OutlinedButton(
                    onClick = onCopyClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.copy_summary_button))
                }
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
                        text = if (stringResource(R.string.language_label) == "Idioma") "Fabricante: ${producto.fabricante}" 
                               else if (stringResource(R.string.language_label) == "Language") "Manufacturer: ${producto.fabricante}"
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
                            "Primario" -> stringResource(R.string.primary_use_label)
                            "Secundario" -> stringResource(R.string.secondary_use_label)
                            else -> "${stringResource(R.string.primary_use_label)} / ${stringResource(R.string.secondary_use_label)}"
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
                        text = stringResource(R.string.clinical_mechanism),
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
                            text = stringResource(R.string.precautions_title),
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
                        text = stringResource(R.string.close_button),
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
    ) {
    val context = LocalContext.current
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
            text = stringResource(R.string.no_match_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_match_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val lang = if (stringResource(R.string.language_label) == "Idioma") "es" else if (stringResource(R.string.language_label) == "Language") "en" else "pt"
            SummaryChip(label = ClinicalTermMapper.translateClinicalTerm(lecho, context))
            SummaryChip(label = ClinicalTermMapper.translateClinicalTerm(exudado, context))
            if (infeccion) {
                SummaryChip(label = stringResource(R.string.infection_chip), isError = true)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.no_match_subtitle2),
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
private fun DisclaimerDialog(onAccept: () -> Unit) {
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
                    text = stringResource(R.string.accept_button),
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
                text = stringResource(R.string.disclaimer_title),
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
                    text = stringResource(R.string.disclaimer_text),
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
fun SettingsDialog(
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit,
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSuggestProductClick: () -> Unit
) {
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
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // --- TEMA ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.theme_label),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("light" to stringResource(R.string.theme_light), "dark" to stringResource(R.string.theme_dark), "system" to stringResource(R.string.theme_system)).forEach { (mode, label) ->
                            val selected = currentTheme == mode
                            FilterChip(
                                selected = selected,
                                onClick = { onThemeChanged(mode) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // --- IDIOMA ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.language_label),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("es" to stringResource(R.string.language_es), "en" to stringResource(R.string.language_en), "pt" to stringResource(R.string.language_pt)).forEach { (lang, label) ->
                            val selected = currentLanguage == lang
                            FilterChip(
                                selected = selected,
                                onClick = { onLanguageChanged(lang) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // --- SUGERENCIAS ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.settings_suggest_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = stringResource(R.string.settings_suggest_desc),
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
                        Text(stringResource(R.string.suggest_product_button))
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
                            text = stringResource(R.string.app_version_label),
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
                        Text(stringResource(R.string.developer_label))
                    }

                    if (showDeveloperInfo) {
                        DeveloperInfoDialog(onDismiss = { showDeveloperInfo = false })
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.donations_label),
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
                        Text(stringResource(R.string.donation_button_text))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.source_code_label),
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
                        text = stringResource(R.string.close_button),
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
                    text = stringResource(R.string.suggest_product_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_field_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = isHealthPro, onCheckedChange = { isHealthPro = it })
                    Text(stringResource(R.string.is_health_pro_label), style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(checked = isLab, onCheckedChange = { isLab = it })
                    Text(stringResource(R.string.is_lab_label), style = MaterialTheme.typography.bodyMedium)
                }

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text(stringResource(R.string.product_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = woundBed,
                    onValueChange = { woundBed = it },
                    label = { Text(stringResource(R.string.product_bed_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = exudateLevel,
                    onValueChange = { exudateLevel = it },
                    label = { Text(stringResource(R.string.product_exudate_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = otherSuggestions,
                    onValueChange = { otherSuggestions = it },
                    label = { Text(stringResource(R.string.other_suggestions_label)) },
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
                        Text(stringResource(R.string.cancel_button))
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
                            Text(stringResource(R.string.form_sending_msg))
                        } else {
                            Text(stringResource(R.string.send_button))
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
                        text = stringResource(R.string.wound_size_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.wound_size_desc),
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
                    label = { Text(stringResource(R.string.wound_length_label)) },
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
                    label = { Text(stringResource(R.string.wound_width_label)) },
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
                    label = { Text(stringResource(R.string.wound_depth_label)) },
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
                        text = stringResource(R.string.cavitation_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.cavitation_desc),
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
                        label = { Text(stringResource(R.string.cavitation_details_label)) },
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
                text = stringResource(R.string.special_location_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.special_location_desc),
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
                text = stringResource(R.string.germ_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = stringResource(R.string.germ_desc),
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
private fun AiResponseCard(isLoading: Boolean, response: String?, ) {
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
                    text = stringResource(R.string.ai_assistant_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analizando lecho y exudado...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = parseSimpleMarkdown(response ?: stringResource(R.string.ai_response_error)),
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
fun DeveloperInfoDialog(onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close_button))
            }
        },
        title = { Text(stringResource(R.string.developer_label)) },
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