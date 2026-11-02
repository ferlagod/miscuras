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

/**
 * Contenido del glosario clínico (GNEAUPP / NPIAP-EPUAP) en los idiomas de la app.
 * Se selecciona según el idioma activo; por defecto, español.
 */
fun glossaryCategories(language: String): List<GlossaryCategory> = when (language.take(2)) {
    "en" -> glossaryEn
    "pt" -> glossaryPt
    else -> glossaryEs
}

private val glossaryEs = listOf(
    GlossaryCategory(
        categoryName = "Categorías de lesiones por presión (GNEAUPP / NPIAP-EPUAP)",
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
            ),
            GlossaryItem(
                title = "No estadiable / Inclasificable",
                description = "Profundidad desconocida",
                details = "Pérdida total del espesor de los tejidos en la que la base de la úlcera está cubierta por esfacelos y/o escara, por lo que no puede determinarse su profundidad real hasta retirarlos. Una vez desbridada, corresponderá a una Categoría III o IV. La escara estable (seca, adherida, sin eritema ni fluctuación) en talones no debe retirarse."
            ),
            GlossaryItem(
                title = "Sospecha de lesión en tejidos profundos",
                description = "Profundidad desconocida bajo piel íntegra",
                details = "Área localizada de color púrpura, granate o marrón oscuro en piel intacta, o flictena con contenido hemático, debida al daño de los tejidos blandos subyacentes por presión y/o cizalla. Puede ir precedida de tejido doloroso, firme, blando, más caliente o más frío. Puede evolucionar rápidamente y dejar expuestas capas adicionales de tejido."
            )
        )
    ),
    GlossaryCategory(
        categoryName = "Lesiones relacionadas con la dependencia (GNEAUPP)",
        items = listOf(
            GlossaryItem(
                title = "Lesiones por presión-cizalla",
                description = "Isquemia por presión mantenida o combinada con cizalla",
                details = "Se producen por la compresión de los tejidos entre una prominencia ósea y una superficie externa, a menudo combinada con fuerzas tangenciales (cizalla) al deslizarse el paciente. Se clasifican por categorías (I-IV)."
            ),
            GlossaryItem(
                title = "Lesiones cutáneas asociadas a la humedad (LESCAH)",
                description = "Daño por exposición prolongada a la humedad",
                details = "Inflamación y erosión de la piel por exposición mantenida a orina, heces, sudor, exudado o saliva. Incluye la dermatitis asociada a incontinencia, la dermatitis intertriginosa (en pliegues), la dermatitis perilesional por exudado y la dermatitis cutánea por exudado o saliva. Suelen ser superficiales, de bordes difusos e irregulares, y no se localizan necesariamente sobre prominencias óseas. Su prevención se basa en higiene suave, control de la humedad y productos barrera."
            ),
            GlossaryItem(
                title = "Lesiones por fricción",
                description = "Roce repetido entre piel y superficie",
                details = "Producidas por el roce o desplazamiento de la piel sobre una superficie (por ejemplo, al arrastrar al paciente). Suelen ser superficiales, con flictenas o erosiones, y aparecen en talones, codos o zonas de apoyo. Se previenen con técnicas de movilización adecuadas y protección local."
            ),
            GlossaryItem(
                title = "Lesiones combinadas y multicausales",
                description = "Concurren varios mecanismos",
                details = "Lesiones en las que actúan a la vez varios factores (por ejemplo, humedad y presión, o humedad y fricción). Se denominan según los mecanismos implicados y su abordaje debe dirigirse a todos ellos."
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

private val glossaryEn = listOf(
    GlossaryCategory(
        categoryName = "Pressure injury categories (GNEAUPP / NPIAP-EPUAP)",
        items = listOf(
            GlossaryItem(
                title = "Category I",
                description = "Non-blanchable erythema",
                details = "Intact skin with non-blanchable redness of a localised area, usually over a bony prominence. Discolouration, warmth, oedema, induration or pain may also be present. It indicates involvement of the epidermis only."
            ),
            GlossaryItem(
                title = "Category II",
                description = "Partial-thickness ulcer",
                details = "Partial-thickness loss of dermis presenting as a shallow open ulcer with a red-pink wound bed, without slough. It may also present as an intact or open/ruptured serum-filled blister. It affects the epidermis and dermis."
            ),
            GlossaryItem(
                title = "Category III",
                description = "Full-thickness skin loss",
                details = "Full-thickness tissue loss. Subcutaneous fat may be visible but bone, tendon or muscle are NOT exposed. Slough may be present. It may include undermining and tunnelling. It affects the epidermis, dermis and subcutaneous tissue."
            ),
            GlossaryItem(
                title = "Category IV",
                description = "Full-thickness tissue loss",
                details = "Full-thickness tissue loss with exposed bone, tendon or muscle. Slough or eschar may be present. It often includes undermining and tunnelling. Depth varies by anatomical location. It affects the epidermis, dermis, subcutaneous tissue, fascia, muscle and/or bone."
            ),
            GlossaryItem(
                title = "Unstageable",
                description = "Depth unknown",
                details = "Full-thickness tissue loss in which the base of the ulcer is covered by slough and/or eschar, so the true depth cannot be determined until they are removed. Once debrided it will be Category III or IV. Stable eschar on the heels (dry, adherent, without erythema or fluctuance) should not be removed."
            ),
            GlossaryItem(
                title = "Suspected deep tissue injury",
                description = "Depth unknown under intact skin",
                details = "Localised purple, maroon or dark brown area of intact skin, or a blood-filled blister, due to damage of the underlying soft tissue from pressure and/or shear. It may be preceded by tissue that is painful, firm, mushy, warmer or cooler. It can evolve rapidly and expose additional tissue layers."
            )
        )
    ),
    GlossaryCategory(
        categoryName = "Dependence-related skin lesions (GNEAUPP)",
        items = listOf(
            GlossaryItem(
                title = "Pressure-shear injuries",
                description = "Ischaemia from sustained pressure, alone or with shear",
                details = "Caused by compression of tissue between a bony prominence and an external surface, often combined with tangential (shear) forces when the patient slides. They are classified by category (I-IV)."
            ),
            GlossaryItem(
                title = "Moisture-associated skin damage (MASD)",
                description = "Damage from prolonged exposure to moisture",
                details = "Inflammation and erosion of the skin from prolonged exposure to urine, faeces, sweat, exudate or saliva. It includes incontinence-associated dermatitis, intertriginous dermatitis (skin folds), periwound dermatitis due to exudate and skin damage from exudate or saliva. Lesions are usually superficial, with diffuse irregular edges, and are not necessarily over bony prominences. Prevention relies on gentle cleansing, moisture control and barrier products."
            ),
            GlossaryItem(
                title = "Friction injuries",
                description = "Repeated rubbing between skin and a surface",
                details = "Caused by rubbing or sliding of the skin over a surface (for example, when dragging the patient). They are usually superficial, with blisters or erosions, on heels, elbows or contact areas. They are prevented with proper handling techniques and local protection."
            ),
            GlossaryItem(
                title = "Combined and multi-causal lesions",
                description = "Several mechanisms at once",
                details = "Lesions in which several factors act together (for example, moisture and pressure, or moisture and friction). They are named after the mechanisms involved and management must address all of them."
            )
        )
    ),
    GlossaryCategory(
        categoryName = "Types of Debridement",
        items = listOf(
            GlossaryItem(
                title = "Autolytic debridement",
                description = "Natural process driven by endogenous enzymes",
                details = "Moisture-retaining dressings (such as hydrogels or hydrocolloids) help the body's own enzymes and phagocytes liquefy necrosis and slough. It is highly selective and painless, but slow. Contraindicated in severe uncontrolled infection."
            ),
            GlossaryItem(
                title = "Enzymatic debridement",
                description = "Topical application of enzymes",
                details = "Enzymatic ointments (such as collagenase) break down the collagen of necrotic tissue. They need a moist environment to work. It is selective and faster than autolytic debridement. It is often combined with cross-hatching of dry eschar to aid penetration."
            ),
            GlossaryItem(
                title = "Sharp / surgical debridement",
                description = "Use of scalpel, scissors or curette",
                details = "Excision of devitalised tissue. It is the fastest method but not selective and requires skill and anatomical knowledge. It removes slough and necrotic tissue from the wound bed. Surgical debridement is done in theatre; conservative sharp debridement is performed by nurses at the bedside. There is a risk of bleeding and pain."
            ),
            GlossaryItem(
                title = "Osmotic debridement",
                description = "Fluid exchange driven by a concentration gradient",
                details = "High-osmolarity substances (such as medical honey or superabsorbent polyacrylate dressings) draw water and fluid from the wound bed into the dressing, carrying away devitalised tissue, bacteria and excess exudate. It helps reduce odour and bacterial load."
            )
        )
    ),
    GlossaryCategory(
        categoryName = "Dressing Families",
        items = listOf(
            GlossaryItem(
                title = "Alginates",
                description = "Seaweed-derived, highly absorbent",
                details = "They absorb moderate to high exudate, forming a hydrophilic gel that keeps the wound moist. They have haemostatic properties (help stop minor bleeding). They need a secondary dressing. Ideal for deep, cavity or bleeding wounds."
            ),
            GlossaryItem(
                title = "Hydrofibres",
                description = "Carboxymethylcellulose (CMC) fibres",
                details = "They absorb large amounts of exudate directly into the fibre, gelling and retaining fluid vertically, which prevents maceration of the wound edges. Excellent for highly exuding wounds. Some include silver (Ag) for infection."
            ),
            GlossaryItem(
                title = "Polyurethane foams",
                description = "Cellular (sponge-like) dressings",
                details = "They provide thermal insulation, mechanical protection (cushioning) and management of moderate to high exudate. They do not adhere to a moist wound bed. Widely used for pressure injuries, venous ulcers and prevention in high-risk areas (heels, sacrum)."
            ),
            GlossaryItem(
                title = "Hydrocolloids",
                description = "Carboxymethylcellulose with adhesives (pectin, gelatin)",
                details = "They form a gel on contact with exudate. They are occlusive or semi-occlusive and promote strong autolytic debridement. Ideal for no or low exudate. Contraindicated in infected or highly exuding wounds, as they can trap bacteria and cause maceration."
            ),
            GlossaryItem(
                title = "Hydrogels",
                description = "High water content (up to 90%)",
                details = "They add moisture to dry wounds (dry necrosis, eschar). They promote autolytic debridement by softening dead tissue. They are soothing and cooling. They need a secondary dressing. Used for no to low exudate."
            )
        )
    )
)

private val glossaryPt = listOf(
    GlossaryCategory(
        categoryName = "Categorias de lesões por pressão (GNEAUPP / NPIAP-EPUAP)",
        items = listOf(
            GlossaryItem(
                title = "Categoria I",
                description = "Eritema não branqueável",
                details = "Pele intacta com eritema não branqueável de uma área localizada, geralmente sobre uma proeminência óssea. Também podem estar presentes descoloração, calor, edema, endurecimento ou dor. Indica atingimento exclusivo da epiderme."
            ),
            GlossaryItem(
                title = "Categoria II",
                description = "Úlcera de espessura parcial",
                details = "Perda de espessura parcial da derme que se apresenta como uma úlcera aberta pouco profunda com leito vermelho-rosado, sem tecido desvitalizado (esfacelo). Pode também apresentar-se como flictena intacta ou aberta/rota com conteúdo seroso. Atinge epiderme e derme."
            ),
            GlossaryItem(
                title = "Categoria III",
                description = "Perda total da espessura da pele",
                details = "Perda total da espessura do tecido. A gordura subcutânea pode estar visível, mas osso, tendão ou músculo NÃO estão expostos. Pode haver esfacelo. Pode incluir cavitações e túneis. Atinge epiderme, derme e tecido subcutâneo."
            ),
            GlossaryItem(
                title = "Categoria IV",
                description = "Perda total da espessura dos tecidos",
                details = "Perda total da espessura do tecido com osso, tendão ou músculo expostos. Pode haver esfacelo ou escara. Inclui frequentemente cavitações e túneis. A profundidade varia com a localização anatómica. Atinge epiderme, derme, tecido subcutâneo, fáscia, músculo e/ou osso."
            ),
            GlossaryItem(
                title = "Não classificável",
                description = "Profundidade desconhecida",
                details = "Perda total da espessura dos tecidos em que a base da úlcera está coberta por esfacelo e/ou escara, pelo que a verdadeira profundidade só pode ser determinada após a sua remoção. Depois de desbridada, corresponderá a Categoria III ou IV. A escara estável nos calcanhares (seca, aderente, sem eritema nem flutuação) não deve ser removida."
            ),
            GlossaryItem(
                title = "Suspeita de lesão nos tecidos profundos",
                description = "Profundidade desconhecida sob pele íntegra",
                details = "Área localizada de cor púrpura, castanho-avermelhada ou castanho-escura em pele intacta, ou flictena com conteúdo hemático, devida a lesão dos tecidos moles subjacentes por pressão e/ou cisalhamento. Pode ser precedida por tecido doloroso, firme, mole, mais quente ou mais frio. Pode evoluir rapidamente e expor camadas adicionais de tecido."
            )
        )
    ),
    GlossaryCategory(
        categoryName = "Lesões relacionadas com a dependência (GNEAUPP)",
        items = listOf(
            GlossaryItem(
                title = "Lesões por pressão-cisalhamento",
                description = "Isquemia por pressão mantida, isolada ou com cisalhamento",
                details = "Produzem-se pela compressão dos tecidos entre uma proeminência óssea e uma superfície externa, muitas vezes combinada com forças tangenciais (cisalhamento) quando o doente desliza. Classificam-se por categorias (I-IV)."
            ),
            GlossaryItem(
                title = "Lesões cutâneas associadas à humidade (LESCAH)",
                description = "Dano por exposição prolongada à humidade",
                details = "Inflamação e erosão da pele por exposição mantida a urina, fezes, suor, exsudado ou saliva. Inclui a dermatite associada à incontinência, a dermatite intertriginosa (nas pregas), a dermatite perilesional por exsudado e a dermatite cutânea por exsudado ou saliva. Costumam ser superficiais, de bordos difusos e irregulares, e não se localizam necessariamente sobre proeminências ósseas. A prevenção baseia-se em higiene suave, controlo da humidade e produtos barreira."
            ),
            GlossaryItem(
                title = "Lesões por fricção",
                description = "Atrito repetido entre a pele e uma superfície",
                details = "Produzidas pelo atrito ou deslizamento da pele sobre uma superfície (por exemplo, ao arrastar o doente). Costumam ser superficiais, com flictenas ou erosões, nos calcanhares, cotovelos ou zonas de apoio. Previnem-se com técnicas de mobilização adequadas e proteção local."
            ),
            GlossaryItem(
                title = "Lesões combinadas e multicausais",
                description = "Vários mecanismos em simultâneo",
                details = "Lesões em que atuam vários fatores ao mesmo tempo (por exemplo, humidade e pressão, ou humidade e fricção). Denominam-se segundo os mecanismos envolvidos e a abordagem deve dirigir-se a todos eles."
            )
        )
    ),
    GlossaryCategory(
        categoryName = "Tipos de Desbridamento",
        items = listOf(
            GlossaryItem(
                title = "Desbridamento autolítico",
                description = "Processo natural mediado por enzimas endógenas",
                details = "Uso de pensos que retêm a humidade (como hidrogéis ou hidrocolóides) para que as próprias enzimas do organismo e os fagócitos liquefaçam a necrose e o esfacelo. É muito seletivo e indolor, mas lento. Contraindicado se existir infeção grave não controlada."
            ),
            GlossaryItem(
                title = "Desbridamento enzimático",
                description = "Aplicação tópica de enzimas",
                details = "Aplicação de pomadas enzimáticas (como a colagenase) que degradam o colagénio do tecido necrótico. Precisa de um ambiente húmido para atuar. É seletivo e mais rápido do que o autolítico. Combina-se muitas vezes com incisões em grelha nas escaras secas para facilitar a penetração."
            ),
            GlossaryItem(
                title = "Desbridamento cortante / cirúrgico",
                description = "Uso de bisturi, tesoura ou cureta",
                details = "Excisão do tecido desvitalizado. É o método mais rápido, mas não é seletivo e exige perícia e conhecimentos de anatomia. Remove esfacelo e tecido necrótico do leito da ferida. O cirúrgico realiza-se no bloco operatório; o cortante é feito pela enfermagem junto ao leito. Existe risco de hemorragia e dor."
            ),
            GlossaryItem(
                title = "Desbridamento osmótico",
                description = "Troca de fluidos por gradiente de concentração",
                details = "Usam-se substâncias de alta osmolaridade (como o mel médico ou pensos superabsorventes de poliacrilato) que atraem água e fluidos do leito da ferida para o penso, arrastando tecido desvitalizado, bactérias e excesso de exsudado. Ajuda a reduzir o odor e a carga bacteriana."
            )
        )
    ),
    GlossaryCategory(
        categoryName = "Famílias de Pensos",
        items = listOf(
            GlossaryItem(
                title = "Alginatos",
                description = "Derivados de algas marinhas, muito absorventes",
                details = "Absorvem exsudado moderado a elevado formando um gel hidrofílico que mantém o ambiente húmido. Têm propriedades hemostáticas (ajudam a parar pequenas hemorragias). Precisam de penso secundário. Ideais para feridas profundas, cavitadas ou sangrantes."
            ),
            GlossaryItem(
                title = "Hidrofibras",
                description = "Fibras de carboximetilcelulose (CMC)",
                details = "Absorvem grandes quantidades de exsudado diretamente na fibra, gelificando e retendo o fluido na vertical, o que evita a maceração dos bordos. Excelentes para feridas muito exsudativas. Algumas incluem prata (Ag) para a infeção."
            ),
            GlossaryItem(
                title = "Espumas de poliuretano",
                description = "Pensos de estrutura celular (esponja)",
                details = "Proporcionam isolamento térmico, proteção mecânica (amortecimento) e gestão do exsudado moderado a elevado. Não aderem ao leito húmido da ferida. Muito usados em lesões por pressão, úlceras venosas e prevenção em zonas de risco (calcanhares, sacro)."
            ),
            GlossaryItem(
                title = "Hidrocolóides",
                description = "Carboximetilcelulose com adesivos (pectina, gelatina)",
                details = "Formam um gel em contacto com o exsudado. São oclusivos ou semioclusivos e promovem um desbridamento autolítico intenso. Ideais para exsudado nulo ou escasso. Contraindicados em feridas infetadas ou muito exsudativas, pois podem reter bactérias e macerar."
            ),
            GlossaryItem(
                title = "Hidrogéis",
                description = "Elevado teor de água (até 90%)",
                details = "Fornecem humidade às feridas secas (necrose seca, escaras). Favorecem o desbridamento autolítico amolecendo o tecido morto. Têm efeito calmante e refrescante. Precisam de penso secundário. Usam-se com exsudado nulo a escasso."
            )
        )
    )
)
