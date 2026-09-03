package com.example.data.repository

import com.example.model.BirdSpecies
import com.example.model.ConservationStatus

object InitialBirdData {
    val defaultBirds: List<BirdSpecies> = listOf(
        BirdSpecies(
            scientificName = "Furnarius rufus",
            commonName = "Hornero",
            familyName = "Furnariidae",
            description = "El Hornero es famoso por ser un arquitecto increíble de la naturaleza. Construye con su pareja un nido redondo de barro y ramitas que parece un horno de pan, muy resistente al viento y la lluvia.\n\nVive en parques, jardines y campos abiertos. Camina con pasitos elegantes buscando pequeños bichitos, lombrices y semillas en el suelo mientras canta a dúo con su compañero.",
            conservationStatus = ConservationStatus.LEAST_CONCERN,
            audioUrl = "https://xeno-canto.org/677840/download",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=800&auto=format&fit=crop&q=80",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Furnarius_rufus_-Costanera_Sur_Ecological_Reserve%2C_Buenos_Aires%2C_Argentina-8.jpg/800px-Furnarius_rufus_-Costanera_Sur_Ecological_Reserve%2C_Buenos_Aires%2C_Argentina-8.jpg"
            ),
            soundDuration = "0:18",
            funFact = "¡Su nido de barro puede pesar hasta 5 kilos y tardan solo 15 días en construirlo!",
            wingspan = "28 - 32 cm",
            diet = "Insectos, lombrices y orugas",
            distanceKm = 0.8,
            isDiscovered = true
        ),
        BirdSpecies(
            scientificName = "Pitangus sulphuratus",
            commonName = "Benteveo",
            familyName = "Tyrannidae",
            description = "Un ave muy pilla y curiosa con un antifaz negro en sus ojos y un pecho de color amarillo brillante como el sol. Le encanta posarse en las ramas más altas para vigilar todo el barrio.\n\nSu nombre viene de su famoso grito: parece decir '¡Bien-te-veo!' con mucha fuerza. Es un cazador acrobático que come desde frutas hasta pececitos que atrapa lanzándose al agua.",
            conservationStatus = ConservationStatus.LEAST_CONCERN,
            audioUrl = "https://xeno-canto.org/671911/download",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1606567595334-d39972c85dbe?w=800&auto=format&fit=crop&q=80",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/Pitangus_sulphuratus_-Costanera_Sur_Ecological_Reserve%2C_Buenos_Aires%2C_Argentina-8.jpg/800px-Pitangus_sulphuratus_-Costanera_Sur_Ecological_Reserve%2C_Buenos_Aires%2C_Argentina-8.jpg"
            ),
            soundDuration = "0:12",
            funFact = "Tiene una corona de plumas amarillas escondida en la cabeza que solo muestra cuando está emocionado.",
            wingspan = "36 - 40 cm",
            diet = "Insectos voladores, frutos y renacuajos",
            distanceKm = 1.2,
            isDiscovered = true
        ),
        BirdSpecies(
            scientificName = "Paroaria coronata",
            commonName = "Cardenal Común",
            familyName = "Thraupidae",
            description = "Reconocible al instante por su llamativo copete de plumas rojo escarlata y su elegante espalda gris plata. Es una de las aves más alegres y sociables de los bosques y plazas.\n\nEmite un canto musical muy dulce y melodioso que se escucha temprano por las mañanas. Pasa mucho tiempo en el suelo buscando semillas junto a su familia.",
            conservationStatus = ConservationStatus.LEAST_CONCERN,
            audioUrl = "https://xeno-canto.org/512140/download",
            photoUrls = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/Paroaria_coronata_-_Red-crested_Cardinal.jpg/800px-Paroaria_coronata_-_Red-crested_Cardinal.jpg",
                "https://images.unsplash.com/photo-1549608276-5786777e6587?w=800&auto=format&fit=crop&q=80"
            ),
            soundDuration = "0:22",
            funFact = "Cuando los pichones nacen, su copete no es rojo sino marrón clarito para camuflarse.",
            wingspan = "25 - 28 cm",
            diet = "Semillas de pastos silvestres y brotes tiernos",
            distanceKm = 2.1,
            isFavorite = true
        ),
        BirdSpecies(
            scientificName = "Turdus rufiventris",
            commonName = "Zorzal Colorado",
            familyName = "Turdidae",
            description = "El gran tenor de los árboles urbanos. Tiene una panza de color naranja rojizo y un pico amarillo verdoso. Es el primer pájaro en saludar con su canto antes de que salga el sol.\n\nEs un maestro buscando lombrices en la tierra húmeda después de llover. Salta dos pasos, inclina la cabeza para escuchar a las lombrices bajo tierra y ¡zas!, las atrapa con su pico.",
            conservationStatus = ConservationStatus.LEAST_CONCERN,
            audioUrl = "https://xeno-canto.org/769854/download",
            photoUrls = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/Turdus_rufiventris_-_Rufous-bellied_Thrush.jpg/800px-Turdus_rufiventris_-_Rufous-bellied_Thrush.jpg",
                "https://images.unsplash.com/photo-1444464666168-49d633b86797?w=800&auto=format&fit=crop&q=80"
            ),
            soundDuration = "0:25",
            funFact = "Es el ave nacional de Brasil y una de las voces más queridas en toda América del Sur.",
            wingspan = "34 - 38 cm",
            diet = "Lombrices de tierra, caracoles y moras",
            distanceKm = 0.5
        ),
        BirdSpecies(
            scientificName = "Harpia harpyja",
            commonName = "Águila Arpía",
            familyName = "Accipitridae",
            description = "Es una de las águilas más poderosas y gigantes del planeta. Posee unas garras tan grandes como las de un oso pardo y una doble corona de plumas que levanta cuando está alerta.\n\nVive en lo más alto de las selvas tropicales vírgenes. Debido a la tala de árboles y la pérdida de sus bosques, hoy necesita de nuestra máxima protección para no desaparecer.",
            conservationStatus = ConservationStatus.VULNERABLE,
            audioUrl = "https://xeno-canto.org/426980/download",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1611689342806-0863700ce1e4?w=800&auto=format&fit=crop&q=80",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Harpia_harpyja_01.jpg/800px-Harpia_harpyja_01.jpg"
            ),
            soundDuration = "0:14",
            funFact = "Sus alas pueden medir más de 2 metros de punta a punta para maniobrar entre ramas densas.",
            wingspan = "176 - 224 cm",
            diet = "Monos, perezosos e iguanas de gran tamaño",
            distanceKm = 14.5
        ),
        BirdSpecies(
            scientificName = "Spheniscus magellanicus",
            commonName = "Pingüino de Magallanes",
            familyName = "Spheniscidae",
            description = "Un simpático buceador vestido con traje de fiesta blanco y negro. En la tierra camina balanceándose graciosamente, pero bajo el agua parece volar a toda velocidad como un torpedo.\n\nCría a sus pichones en cuevas de arena cerca del mar patagónico. Se comunican con graznidos fuertes que suenan parecido al rebuzno de un burrito para encontrarse entre miles de pingüinos.",
            conservationStatus = ConservationStatus.NEAR_THREATENED,
            audioUrl = "https://xeno-canto.org/632941/download",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1598439210625-5067c578f3f6?w=800&auto=format&fit=crop&q=80",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d2/Spheniscus_magellanicus%2C_Punta_Tombo%2C_Argentina.jpg/800px-Spheniscus_magellanicus%2C_Punta_Tombo%2C_Argentina.jpg"
            ),
            soundDuration = "0:20",
            funFact = "Pueden nadar miles de kilómetros en el océano abierto sin tocar tierra durante meses.",
            wingspan = "45 - 50 cm (aletas)",
            diet = "Anchoitas, sardinas y pequeños calamares",
            distanceKm = 28.0
        ),
        BirdSpecies(
            scientificName = "Chlorostilbon lucidus",
            commonName = "Picaflor Común",
            familyName = "Trochilidae",
            description = "Una diminuta joya verde brillante que parece flotar en el aire como por arte de magia. Sus alitas baten tan rápido que hacen un zumbido parecido al de una pequeña abeja.\n\nVisita flores coloridas en plazas y jardines para beber su rico néctar con su largo pico curvo. Es la única ave del mundo capaz de volar hacia atrás y de cabeza.",
            conservationStatus = ConservationStatus.LEAST_CONCERN,
            audioUrl = "https://xeno-canto.org/769855/download",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1518992028580-6d97bdca06c3?w=800&auto=format&fit=crop&q=80",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Glittering-bellied_Emerald_%28Chlorostilbon_lucidus%29.jpg/800px-Glittering-bellied_Emerald_%28Chlorostilbon_lucidus%29.jpg"
            ),
            soundDuration = "0:09",
            funFact = "Su corazón late hasta 1.200 veces por minuto mientras bate sus alas 80 veces por segundo.",
            wingspan = "10 - 12 cm",
            diet = "Néctar de flores y pequeños mosquitos",
            distanceKm = 0.3,
            isFavorite = true
        ),
        BirdSpecies(
            scientificName = "Vanellus chilensis",
            commonName = "Tero",
            familyName = "Charadriidae",
            description = "El guardián más ruidoso y valiente del pastizal. Tiene largas patas rojas, un copete rebelde y unos espolones ocultos en sus alas que usa para defender su nido en el suelo.\n\nEs súper inteligente: cuando alguien se acerca a sus huevitos, simula tener el nido en otro lado y grita '¡teru-teru!' muy lejos para despistar a los intrusos.",
            conservationStatus = ConservationStatus.LEAST_CONCERN,
            audioUrl = "https://xeno-canto.org/512141/download",
            photoUrls = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Southern_Lapwing_%28Vanellus_chilensis_lampronotus%29.jpg/800px-Southern_Lapwing_%28Vanellus_chilensis_lampronotus%29.jpg",
                "https://images.unsplash.com/photo-1520808663317-647b476a81b9?w=800&auto=format&fit=crop&q=80"
            ),
            soundDuration = "0:16",
            funFact = "Pone sus huevos directamente sobre el pasto corto con un camuflaje que los hace invisibles.",
            wingspan = "70 - 80 cm",
            diet = "Gusanos, escarabajos y pequeños crustáceos",
            distanceKm = 1.7
        ),
        BirdSpecies(
            scientificName = "Cyanoliseus patagonus",
            commonName = "Loro Barranquero",
            familyName = "Psittacidae",
            description = "Un loro simpático, colorido y muy charlatán de plumas verde oliva, panza roja y alas azul turquesa. Vuela siempre en grandes bandadas llenas de alegría y bullicio.\n\nExcava profundas cuevas en barrancos de tierra o acantilados de piedra junto al río para vivir con sus amigos y proteger a sus pichones del frío.",
            conservationStatus = ConservationStatus.LEAST_CONCERN,
            audioUrl = "https://xeno-canto.org/671912/download",
            photoUrls = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Cyanoliseus_patagonus_-Argentina-8.jpg/800px-Cyanoliseus_patagonus_-Argentina-8.jpg",
                "https://images.unsplash.com/photo-1552053831-71594a27632d?w=800&auto=format&fit=crop&q=80"
            ),
            soundDuration = "0:19",
            funFact = "Eligen a una sola pareja para toda la vida y pueden cavar túneles de hasta 3 metros de largo.",
            wingspan = "70 - 75 cm",
            diet = "Frutos de calafate, semillas de piquillín y brotes",
            distanceKm = 3.4
        ),
        BirdSpecies(
            scientificName = "Phoenicopterus chilensis",
            commonName = "Flamenco Austral",
            familyName = "Phoenicopteridae",
            description = "Un ave majestuosa con plumas de color rosa pastel y larguísimas patas que parecen zancos. Se reúne en lagunas saladas y humedales formando enormes grupos rosados.\n\nPara alimentarse, mete la cabeza bajo el agua al revés y usa su pico como un colador mágico para filtrar diminutos camarones y algas que le dan su color rosa.",
            conservationStatus = ConservationStatus.NEAR_THREATENED,
            audioUrl = "https://xeno-canto.org/426982/download",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1539667547529-84c607280d20?w=800&auto=format&fit=crop&q=80",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/2/23/Chilean_Flamingo_%28Phoenicopterus_chilensis%29_-two.jpg/800px-Chilean_Flamingo_%28Phoenicopterus_chilensis%29_-two.jpg"
            ),
            soundDuration = "0:15",
            funFact = "Los flamencos nacen con plumaje gris y se vuelven rosas gracias a lo que comen.",
            wingspan = "120 - 140 cm",
            diet = "Pequeños camarones de laguna y microalgas",
            distanceKm = 8.6
        ),
        BirdSpecies(
            scientificName = "Athene cunicularia",
            commonName = "Lechuza Vizcachera",
            familyName = "Strigidae",
            description = "Una pequeña lechuza de enormes ojos dorados y mirada curiosa. A diferencia de otras lechuzas nocturnas, a ella le encanta tomar sol en el pasto durante el día.\n\nVive en cuevas subterráneas que a veces le prestan las vizcachas o armadillos. Cuando se siente amenazada, hace un sonido parecido al cascabel de una serpiente para asustar.",
            conservationStatus = ConservationStatus.LEAST_CONCERN,
            audioUrl = "https://xeno-canto.org/769856/download",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1543549790-8b5f4a028cfb?w=800&auto=format&fit=crop&q=80",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Burrowing_Owl_-_Athene_cunicularia.jpg/800px-Burrowing_Owl_-_Athene_cunicularia.jpg"
            ),
            soundDuration = "0:11",
            funFact = "Puede girar su cabeza casi por completo (270 grados) sin mover el resto de su cuerpo.",
            wingspan = "50 - 55 cm",
            diet = "Grillos, langostas, lagartijas y ratoncitos",
            distanceKm = 4.2
        ),
        BirdSpecies(
            scientificName = "Campephilus magellanicus",
            commonName = "Carpintero Gigante",
            familyName = "Picidae",
            description = "El rey indiscutido de los bosques patagónicos. El macho tiene una cabeza totalmente roja como una llama de fuego y un cuerpo negro lustroso con plumas blancas en las alas.\n\nTamborilea los troncos de los árboles viejos con golpes dobles tan potentes que se escuchan a kilómetros de distancia, buscando larvas ocultas en la madera.",
            conservationStatus = ConservationStatus.LEAST_CONCERN,
            audioUrl = "https://xeno-canto.org/671914/download",
            photoUrls = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Campephilus_magellanicus_-Tierra_del_Fuego_National_Park-8_%28cropped%29.jpg/800px-Campephilus_magellanicus_-Tierra_del_Fuego_National_Park-8_%28cropped%29.jpg",
                "https://images.unsplash.com/photo-1590682680695-43b964a3ae17?w=800&auto=format&fit=crop&q=80"
            ),
            soundDuration = "0:21",
            funFact = "Tiene un cráneo con amortiguación especial para no marearse al martillar los árboles.",
            wingspan = "70 - 75 cm",
            diet = "Grandes larvas e insectos de la madera",
            distanceKm = 18.0
        ),
        BirdSpecies(
            scientificName = "Sporophila cinnamomea",
            commonName = "Capuchino Corona Gris",
            familyName = "Thraupidae",
            description = "Un diminuto pajarito cantor de plumaje canela rojizo y gorrito plateado. Realiza asombrosos viajes migratorios cada año a través de los pastizales naturales de América del Sur.\n\nSe alimenta de semillas de pastos nativos posándose con gracia en las espigas. Está en peligro porque sus pastizales han sido transformados en campos de cultivo.",
            conservationStatus = ConservationStatus.VULNERABLE,
            audioUrl = "https://xeno-canto.org/512143/download",
            photoUrls = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Chestnut_Seedeater_%28Sporophila_cinnamomea%29.jpg/800px-Chestnut_Seedeater_%28Sporophila_cinnamomea%29.jpg",
                "https://images.unsplash.com/photo-1452570053594-1b985d6ea890?w=800&auto=format&fit=crop&q=80"
            ),
            soundDuration = "0:14",
            funFact = "Pesa menos de 9 gramos, ¡lo mismo que una moneda o dos cucharaditas de azúcar!",
            wingspan = "14 - 16 cm",
            diet = "Semillas de pastos naturales silvestres",
            distanceKm = 11.2
        ),
        BirdSpecies(
            scientificName = "Guaruba guarouba",
            commonName = "Aratinga Dorada",
            familyName = "Psittacidae",
            description = "Un loro deslumbrante de plumas amarillo dorado intenso con las puntas de las alas verdes como esmeraldas. Es una de las aves más hermosas y alegres de la selva amazónica.\n\nForma grupos familiares muy cariñosos donde todos los adultos ayudan a cuidar y alimentar a los pichones de la colonia.",
            conservationStatus = ConservationStatus.VULNERABLE,
            audioUrl = "https://xeno-canto.org/769857/download",
            photoUrls = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Golden_Parakeet_%28Guaruba_guarouba%29_-pair.jpg/800px-Golden_Parakeet_%28Guaruba_guarouba%29_-pair.jpg",
                "https://images.unsplash.com/photo-1552053831-71594a27632d?w=800&auto=format&fit=crop&q=80"
            ),
            soundDuration = "0:17",
            funFact = "En Brasil se le conoce como 'Ararajuba', que en idioma indígena tupí significa 'loro amarillo'.",
            wingspan = "55 - 60 cm",
            diet = "Frutas tropicales, nueces y flores silvestres",
            distanceKm = 22.0
        )
    )
}
