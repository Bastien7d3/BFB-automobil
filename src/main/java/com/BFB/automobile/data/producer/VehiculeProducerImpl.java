package com.BFB.automobile.data.producer;

import com.BFB.automobile.data.Vehicule;
import org.springframework.stereotype.Component;

/**
 * COUCHE STOCKAGE - Implémentation du Producer
 * Simulation de communication avec des systèmes externes.
 */
@Component
public class VehiculeProducerImpl implements VehiculeProducer {
    
    @Override
    public void publierVehicule(Vehicule vehicule) {
        // Simulation: dans un vrai projet, envoi vers Kafka, RabbitMQ, etc.
        System.out.println("📤 Publication véhicule: " + vehicule.getMarque() + " " + vehicule.getModele());
    }
    
    @Override
    public Double obtenirCotation(String marque, String modele, Integer annee) {
        // Simulation: dans un vrai projet, appel API externe de cotation
        System.out.println("🔍 Cotation pour: " + marque + " " + modele);
        return 15000.0;
    }
}
