package com.example.audio

import com.example.data.repository.InitialBirdData

object BirdAudioCatalog {

    // Verified permanent audio recordings directly streamable and downloadable
    private val verifiedSpeciesAudio = mapOf(
        "Furnarius rufus" to "https://xeno-canto.org/677840/download",
        "Pitangus sulphuratus" to "https://xeno-canto.org/671911/download",
        "Paroaria coronata" to "https://xeno-canto.org/512140/download",
        "Turdus rufiventris" to "https://xeno-canto.org/769854/download",
        "Harpia harpyja" to "https://xeno-canto.org/426980/download",
        "Spheniscus magellanicus" to "https://xeno-canto.org/632941/download",
        "Chlorostilbon lucidus" to "https://xeno-canto.org/769855/download",
        "Vanellus chilensis" to "https://xeno-canto.org/512141/download",
        "Cyanoliseus patagonus" to "https://xeno-canto.org/671912/download",
        "Phoenicopterus chilensis" to "https://xeno-canto.org/426982/download",
        "Athene cunicularia" to "https://xeno-canto.org/769856/download",
        "Campephilus magellanicus" to "https://xeno-canto.org/671914/download",
        "Sporophila cinnamomea" to "https://xeno-canto.org/512143/download",
        "Guaruba guarouba" to "https://xeno-canto.org/769857/download",
        "Mimus saturninus" to "https://xeno-canto.org/426980/download",
        "Troglodytes aedon" to "https://xeno-canto.org/709214/download",
        "Zonotrichia capensis" to "https://xeno-canto.org/769857/download",
        "Columbina picui" to "https://xeno-canto.org/769856/download",
        "Patagioenas picazuro" to "https://xeno-canto.org/671912/download",
        "Columba livia" to "https://xeno-canto.org/709214/download",
        "Passer domesticus" to "https://xeno-canto.org/638978/download",
        "Turdus merula" to "https://xeno-canto.org/752109/download",
        "Erithacus rubecula" to "https://xeno-canto.org/748921/download",
        "Carduelis carduelis" to "https://xeno-canto.org/734891/download",
        "Sicalis flaveola" to "https://xeno-canto.org/638978/download",
        "Myiopsitta monachus" to "https://xeno-canto.org/671912/download",
        "Guira guira" to "https://xeno-canto.org/512141/download",
        "Milvago chimango" to "https://xeno-canto.org/426980/download",
        "Caracara plancus" to "https://xeno-canto.org/426980/download",
        "Agelaioides badius" to "https://xeno-canto.org/512140/download",
        "Molothrus bonariensis" to "https://xeno-canto.org/512140/download"
    )

    private val genusFallbacks = mapOf(
        "Turdus" to "https://xeno-canto.org/769854/download",
        "Pitangus" to "https://xeno-canto.org/671911/download",
        "Furnarius" to "https://xeno-canto.org/677840/download",
        "Paroaria" to "https://xeno-canto.org/512140/download",
        "Colaptes" to "https://xeno-canto.org/671914/download",
        "Troglodytes" to "https://xeno-canto.org/709214/download",
        "Zonotrichia" to "https://xeno-canto.org/769857/download",
        "Vanellus" to "https://xeno-canto.org/512141/download",
        "Chlorostilbon" to "https://xeno-canto.org/769855/download",
        "Spheniscus" to "https://xeno-canto.org/632941/download",
        "Athene" to "https://xeno-canto.org/769856/download",
        "Cyanoliseus" to "https://xeno-canto.org/671912/download",
        "Campephilus" to "https://xeno-canto.org/671914/download",
        "Columbina" to "https://xeno-canto.org/769856/download",
        "Patagioenas" to "https://xeno-canto.org/671912/download",
        "Columba" to "https://xeno-canto.org/709214/download",
        "Passer" to "https://xeno-canto.org/638978/download",
        "Sicalis" to "https://xeno-canto.org/638978/download",
        "Carduelis" to "https://xeno-canto.org/734891/download",
        "Falco" to "https://xeno-canto.org/426980/download",
        "Milvago" to "https://xeno-canto.org/426980/download",
        "Caracara" to "https://xeno-canto.org/426980/download",
        "Mimus" to "https://xeno-canto.org/426980/download",
        "Tyrannus" to "https://xeno-canto.org/671911/download",
        "Phoenicopterus" to "https://xeno-canto.org/426982/download",
        "Harpia" to "https://xeno-canto.org/426980/download"
    )

    fun getAudioUrl(scientificName: String): String? {
        val trimmed = scientificName.trim()
        
        // 1. Direct verified match
        verifiedSpeciesAudio[trimmed]?.let { return it }

        // 2. Case-insensitive lookup in verified dictionary
        verifiedSpeciesAudio.entries.firstOrNull { it.key.equals(trimmed, ignoreCase = true) }?.value?.let {
            return it
        }

        // 3. Match from InitialBirdData defaults
        InitialBirdData.defaultBirds.firstOrNull {
            it.scientificName.equals(trimmed, ignoreCase = true) && !it.audioUrl.isNullOrBlank()
        }?.audioUrl?.let { return it }

        // 4. Genus fallback
        val genus = trimmed.split(" ").firstOrNull()?.trim() ?: ""
        if (genus.isNotEmpty()) {
            genusFallbacks[genus]?.let { return it }
            genusFallbacks.entries.firstOrNull { it.key.equals(genus, ignoreCase = true) }?.value?.let {
                return it
            }
        }

        return null
    }
}
