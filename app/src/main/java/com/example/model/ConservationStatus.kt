package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.IucnCriticallyEndangered
import com.example.ui.theme.IucnDataDeficient
import com.example.ui.theme.IucnEndangered
import com.example.ui.theme.IucnLeastConcern
import com.example.ui.theme.IucnNatureVulnerable
import com.example.ui.theme.IucnNearThreatened

enum class ConservationStatus(
    val code: String,
    val labelEs: String,
    val descriptionEs: String,
    val color: Color,
    val isThreatened: Boolean
) {
    CRITICALLY_ENDANGERED(
        code = "CR",
        labelEs = "En Peligro Crítico",
        descriptionEs = "Enfrenta un riesgo extremadamente alto de extinción en la naturaleza.",
        color = IucnCriticallyEndangered,
        isThreatened = true
    ),
    ENDANGERED(
        code = "EN",
        labelEs = "En Peligro",
        descriptionEs = "Enfrenta un riesgo muy alto de extinción en estado silvestre.",
        color = IucnEndangered,
        isThreatened = true
    ),
    VULNERABLE(
        code = "VU",
        labelEs = "Vulnerable",
        descriptionEs = "Enfrenta un alto riesgo de extinción en la naturaleza a mediano plazo.",
        color = IucnNatureVulnerable,
        isThreatened = true
    ),
    NEAR_THREATENED(
        code = "NT",
        labelEs = "Casi Amenazado",
        descriptionEs = "Especie cercana a ser clasificada en una categoría de amenaza.",
        color = IucnNearThreatened,
        isThreatened = true
    ),
    LEAST_CONCERN(
        code = "LC",
        labelEs = "Preocupación Menor",
        descriptionEs = "Especie abundante y ampliamente distribuida sin peligro inmediato.",
        color = IucnLeastConcern,
        isThreatened = false
    ),
    DATA_DEFICIENT(
        code = "DD",
        labelEs = "Datos Insuficientes",
        descriptionEs = "No hay suficiente información directa o indirecta sobre su población.",
        color = IucnDataDeficient,
        isThreatened = false
    );

    companion object {
        fun fromCode(code: String?): ConservationStatus {
            val cleanCode = code?.trim()?.uppercase() ?: return LEAST_CONCERN
            return when {
                cleanCode.contains("CR") -> CRITICALLY_ENDANGERED
                cleanCode.contains("EN") -> ENDANGERED
                cleanCode.contains("VU") -> VULNERABLE
                cleanCode.contains("NT") -> NEAR_THREATENED
                cleanCode.contains("DD") -> DATA_DEFICIENT
                cleanCode.contains("LC") -> LEAST_CONCERN
                else -> LEAST_CONCERN
            }
        }
    }
}
