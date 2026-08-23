/*
 * Mis Curas
 * Copyright (C) Fernando Lago. 2026
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ferlagod.miscuras.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.ferlagod.miscuras.R

/**
 * Representa un elemento dentro del glosario de términos.
 */
data class GlossaryItem(
    val title: String,
    val description: String,
    val details: String
)

/**
 * Representa una categoría temática del glosario.
 */
data class GlossaryCategory(
    val categoryName: String,
    val items: List<GlossaryItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(
    onBackClick: () -> Unit,
    currentLanguage: String
) {
    
    // Datos hardcodeados según indicaciones GNEAUPP (completos)
    val categories = listOf(
        GlossaryCategory(
            categoryName = "Estadios UPP (GNEAUPP)",
            items = listOf(
                GlossaryItem(
                    title = "Categoría I",
                    description = "Eritema no blanqueante",
                    details = "Piel intacta con eritema no blanqueante de un área localizada, generalmente sobre una prominencia ósea. Decoloración de la piel, calor, edema, endurecimiento o dolor también pueden estar presentes. Indica afectación exclusiva de la Epidermis."
                ),
                GlossaryItem(
                    title = "Categoría II",
                    description = "Úlcera de espesor parcial",
                    details = "Pérdida de espesor parcial de la dermis que se presenta como una úlcera abierta poco profunda con un lecho de la herida rojo-rosado, sin esfacelos. También puede presentarse como una flictena (ampolla) intacta o abierta/rota llena de suero. Afecta Epidermis y Dermis."
                ),
                GlossaryItem(
                    title = "Categoría III",
                    description = "Pérdida total del grosor de la piel",
                    details = "Pérdida completa del grosor del tejido. La grasa subcutánea puede ser visible pero los huesos, tendones o músculos NO están expuestos. Pueden aparecer esfacelos. Puede incluir cavitaciones y tunelizaciones. Afecta Epidermis, Dermis y Tejido Celular Subcutáneo (TCS)."
                ),
                GlossaryItem(
                    title = "Categoría IV",
                    description = "Pérdida total del grosor de los tejidos",
                    details = "Pérdida total del espesor del tejido con hueso, tendón o músculo expuesto. Pueden aparecer esfacelos o escaras. A menudo incluye cavitaciones y tunelizaciones. La profundidad varía según la ubicación anatómica. Afecta Epidermis, Dermis, TCS, Fascia, Músculo y/o Hueso."
                )
            )
        ),
        GlossaryCategory(
            categoryName = "Tipos de Desbridamiento",
            items = listOf(
                GlossaryItem(
                    title = "Desbridamiento Autolítico",
                    description = "Proceso natural mediado por enzimas endógenas",
                    details = "Uso de apósitos que retienen la humedad (como hidrogeles o hidrocoloides) para facilitar que las propias enzimas del cuerpo y los fagocitos licúen la necrosis y los esfacelos. Es muy selectivo, indoloro, pero lento. Contraindicado si hay infección severa no controlada."
                ),
                GlossaryItem(
                    title = "Desbridamiento Enzimático",
                    description = "Aplicación tópica de enzimas",
                    details = "Consiste en la aplicación de pomadas enzimáticas (como la Colagenasa) que degradan el colágeno del tejido necrótico. Requiere un ambiente húmedo para actuar. Es selectivo y más rápido que el autolítico. A menudo se combina con cortes transversales (en rejilla) en escaras secas para facilitar la penetración."
                ),
                GlossaryItem(
                    title = "Desbridamiento Cortante / Quirúrgico",
                    description = "Uso de bisturí, tijeras o cureta",
                    details = "Es la escisión del tejido desvitalizado. Es el método más rápido pero no es selectivo y requiere habilidad y conocimientos de anatomía. Se realiza en el lecho de la herida para eliminar esfacelos y tejido necrótico. El quirúrgico se hace en quirófano; el cortante lo hace enfermería a pie de cama. Existe riesgo de sangrado y dolor."
                ),
                GlossaryItem(
                    title = "Desbridamiento Osmótico",
                    description = "Intercambio de fluidos por gradiente de concentración",
                    details = "Se utilizan sustancias con alta osmolaridad (como la Miel Médica o apósitos de poliacrilato superabsorbente) que atraen agua y fluidos del lecho de la herida hacia el apósito, arrastrando el tejido desvitalizado, bacterias y exceso de exudado. Ayuda a reducir el mal olor y la carga bacteriana."
                )
            )
        ),
        GlossaryCategory(
            categoryName = "Familias de Apósitos",
            items = listOf(
                GlossaryItem(
                    title = "Alginatos",
                    description = "Derivados de algas marinas, altamente absorbentes",
                    details = "Absorben moderado a alto exudado formando un gel hidrofílico que mantiene el ambiente húmedo. Tienen propiedades hemostáticas (ayudan a parar sangrados leves). Requieren un apósito secundario. Ideales para heridas profundas, cavitadas o sangrantes."
                ),
                GlossaryItem(
                    title = "Hidrofibras",
                    description = "Fibras de carboximetilcelulosa (CMC)",
                    details = "Absorben grandes cantidades de exudado directamente dentro de la fibra, gelificando y reteniendo el fluido de forma vertical, lo que evita la maceración de los bordes perilesionales. Excelentes para heridas altamente exudativas. Algunas incluyen Plata (Ag) para infección."
                ),
                GlossaryItem(
                    title = "Espumas de Poliuretano (Foam)",
                    description = "Apósitos de estructura celular (esponja)",
                    details = "Proporcionan absorción térmica, protección mecánica (amortiguación) y gestión del exudado moderado-alto. No se adhieren al lecho de la herida húmedo. Muy usados en úlceras por presión, úlceras venosas y prevención en zonas de riesgo (talones, sacro)."
                ),
                GlossaryItem(
                    title = "Hidrocoloides",
                    description = "Carboximetilcelulosa con adhesivos (pectina, gelatina)",
                    details = "Forman un gel al entrar en contacto con el exudado. Son oclusivos o semi-oclusivos, promoviendo el desbridamiento autolítico agresivo. Ideales para exudado nulo o bajo. Contraindicados en heridas infectadas o muy exudativas, ya que pueden retener bacterias y macerar."
                ),
                GlossaryItem(
                    title = "Hidrogeles",
                    description = "Alto contenido en agua (hasta un 90%)",
                    details = "Aportan humedad a las heridas secas (necrosis seca, escaras). Fomentan el desbridamiento autolítico ablandando el tejido muerto. Tienen efecto calmante y refrescante. Requieren apósito secundario. Se usan en exudado nulo a escaso."
                )
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.glossary_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.glossary_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.glossary_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            categories.forEach { category ->
                item {
                    Text(
                        text = category.categoryName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                items(category.items) { item ->
                    GlossaryAccordion(item = item, )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun GlossaryAccordion(item: GlossaryItem, ) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) stringResource(R.string.glossary_collapse) else stringResource(R.string.glossary_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
