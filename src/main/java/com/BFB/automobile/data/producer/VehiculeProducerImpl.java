package com.BFB.automobile.data.producer;

import com.BFB.automobile.data.Vehicule;
import org.springframework.stereotype.Component;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * COUCHE STOCKAGE - Implémentation du Producer
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * PATTERN: Stub / Mock Implementation (pour la démo)
 * 
 * POURQUOI CETTE IMPLÉMENTATION:
 * - C'est une simulation pour la démo (pas de vrai système externe)
 * - Dans un vrai projet, on remplacerait par:
 *   → Kafka Producer (spring-kafka)
 *   → RestTemplate ou WebClient pour appels API
 *   → RabbitMQ Publisher (spring-amqp)
 * 
 * AVANTAGE DE L'INTERFACE:
 * - On peut facilement changer cette implémentation
 * - Le service ne dépend que de l'interface VehiculeProducer
 * - Facilite les tests (on peut mocker l'interface)
 * 
 * @Component: Spring crée un bean singleton de cette classe
 * Quand le service demande VehiculeProducer, Spring injecte cette implémentation
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Component // PATTERN: Component Stereotype - Bean Spring injectable
public class VehiculeProducerImpl implements VehiculeProducer {
    
    /**
     * ========== IMPLÉMENTATION: Publication vers système externe ==========
     * SIMULATION: affichage console
     * 
     * DANS UN VRAI PROJET, on ferait:
     * 
     * EXEMPLE KAFKA:
     * @Autowired
     * private KafkaTemplate<String, Vehicule> kafkaTemplate;
     * 
     * public void publierVehicule(Vehicule vehicule) {
     *     kafkaTemplate.send("vehicules-topic", vehicule.getId(), vehicule);
     * }
     * 
     * EXEMPLE API REST:
     * @Autowired
     * private RestTemplate restTemplate;
     * 
     * public void publierVehicule(Vehicule vehicule) {
     *     restTemplate.postForObject(
     *         "https://api.externe.com/vehicules", 
     *         vehicule, 
     *         Void.class
     *     );
     * }
     */
    @Override
    public void publierVehicule(Vehicule vehicule) {
        // SIMULATION: dans un vrai projet, ici on enverrait vers Kafka, RabbitMQ, etc.
        System.out.println("📤 Publication du véhicule vers système externe: " 
            + vehicule.getMarque() + " " + vehicule.getModele());
        
        // TODO en production: implémenter vraie publication (Kafka, RabbitMQ, webhook...)
    }
    
    /**
     * ========== IMPLÉMENTATION: Récupération cotation externe ==========
     * SIMULATION: retourne valeur fixe
     * 
     * DANS UN VRAI PROJET, on ferait:
     * 
     * EXEMPLE API REST:
     * @Autowired
     * private WebClient webClient;
     * 
     * public Double obtenirCotation(String marque, String modele, Integer annee) {
     *     return webClient.get()
     *         .uri("https://api-cotation.com/vehicules?marque={m}&modele={mo}&annee={a}",
     *              marque, modele, annee)
     *         .retrieve()
     *         .bodyToMono(CotationResponse.class)
     *         .map(CotationResponse::getPrix)
     *         .block();
     * }
     * 
     * GESTION ERREURS:
     * - Timeout si API ne répond pas
     * - Fallback valeur par défaut
     * - Circuit Breaker (Resilience4j)
     */
    @Override
    public Double obtenirCotation(String marque, String modele, Integer annee) {
        // SIMULATION: dans un vrai projet, appel à une API externe de cotation
        System.out.println("🔍 Récupération cotation pour: " + marque + " " + modele);
        
        // TODO en production: appeler vraie API de cotation
        return 15000.0; // Valeur fictive pour la démo
    }
}
