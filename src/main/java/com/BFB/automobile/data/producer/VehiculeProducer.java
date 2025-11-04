package com.BFB.automobile.data.producer;

import com.BFB.automobile.data.Vehicule;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * COUCHE STOCKAGE - Producer / Gateway
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * PATTERNS UTILISÉS:
 * 
 * 1. Gateway Pattern (ou Producer Pattern)
 *    - Interface pour communiquer avec des systèmes externes
 *    - Abstraction des appels vers APIs, message queues, etc.
 *    - Découple l'application des détails d'implémentation externes
 *    POURQUOI: on peut changer l'API externe sans toucher au service
 * 
 * 2. Interface Segregation (SOLID)
 *    - Interface dédiée à la communication externe
 *    - Séparation du repository (MongoDB) et du producer (systèmes externes)
 *    POURQUOI: chaque interface a une responsabilité unique
 * 
 * 3. Anti-Corruption Layer
 *    - Protège notre domaine des changements externes
 *    - Traduction entre notre modèle et les APIs externes
 *    POURQUOI: si l'API externe change, seul le producer est impacté
 * 
 * RESPONSABILITÉS DE CETTE COUCHE:
 * ✅ Communication avec systèmes externes (APIs REST, Kafka, RabbitMQ, etc.)
 * ✅ Envoi de notifications/événements
 * ✅ Récupération de données depuis sources externes
 * ❌ PAS de logique métier (le service décide quand appeler le producer)
 * ❌ PAS d'accès direct à MongoDB (c'est le rôle du repository)
 * 
 * EXEMPLES D'UTILISATION RÉELLE:
 * - Publier un événement dans Kafka quand un véhicule est créé
 * - Appeler une API de cotation automobile pour valider le prix
 * - Envoyer un email de notification via un service externe
 * - Synchroniser avec un système de facturation
 * ═══════════════════════════════════════════════════════════════════════════
 */
public interface VehiculeProducer {
    
    /**
     * ========== MÉTHODE: Publication d'événement ==========
     * PATTERN: Event Publishing / Message Producer
     * 
     * POURQUOI:
     * - Notifier d'autres systèmes qu'un véhicule a été créé
     * - Architecture événementielle (Event-Driven Architecture)
     * 
     * EXEMPLES RÉELS:
     * - Publier dans Kafka topic "vehicules.created"
     * - Envoyer dans RabbitMQ queue "notifications"
     * - Appeler webhook d'un système de facturation
     * 
     * @param vehicule Le véhicule à publier vers les systèmes externes
     */
    void publierVehicule(Vehicule vehicule);
    
    /**
     * ========== MÉTHODE: Récupération de données externes ==========
     * PATTERN: External Service Gateway
     * 
     * POURQUOI:
     * - Enrichir nos données avec des infos externes
     * - Valider le prix par rapport au marché
     * 
     * EXEMPLES RÉELS:
     * - Appel API REST vers service de cotation automobile (Argus, etc.)
     * - Récupération prix moyen depuis une API publique
     * - Consultation d'une base de données externe de référence
     * 
     * @param marque La marque du véhicule
     * @param modele Le modèle du véhicule
     * @param annee L'année du véhicule
     * @return La cotation estimée (prix de référence)
     */
    Double obtenirCotation(String marque, String modele, Integer annee);
}
