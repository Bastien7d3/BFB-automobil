package com.BFB.automobile.data.producer;

import com.BFB.automobile.data.Vehicule;

/**
 * COUCHE STOCKAGE - Producer
 * Interface de communication avec des systèmes externes.
 */
public interface VehiculeProducer {
    
    void publierVehicule(Vehicule vehicule);
    Double obtenirCotation(String marque, String modele, Integer annee);
}
