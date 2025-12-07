package com.BFB.automobile.data;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests du Builder Pattern pour Client
 * Démontre l'utilisation du pattern BUILDER
 */
class ClientBuilderTest {
    
    @Test
    void builder_devraitCreerClient_avecTousLesChamps() {
        // Arrange & Act - Utilisation du BUILDER PATTERN
        Client client = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("123456789")
                .adresse("10 rue de la Paix, 75001 Paris")
                .actif(true)
                .build();
        
        // Assert
        assertNotNull(client);
        assertEquals("Dupont", client.getNom());
        assertEquals("Jean", client.getPrenom());
        assertEquals(LocalDate.of(1990, 5, 15), client.getDateNaissance());
        assertEquals("123456789", client.getNumeroPermis());
        assertEquals("10 rue de la Paix, 75001 Paris", client.getAdresse());
        assertTrue(client.getActif());
        assertNotNull(client.getDateCreation());
    }
    
    @Test
    void builder_devraitCreerClient_avecValeursParDefaut() {
        // Arrange & Act - Le builder permet d'omettre certains champs
        Client client = Client.builder()
                .nom("Martin")
                .prenom("Sophie")
                .dateNaissance(LocalDate.of(1985, 8, 20))
                .numeroPermis("987654321")
                .adresse("25 avenue des Champs, 69000 Lyon")
                // actif non spécifié, utilise la valeur par défaut
                .build();
        
        // Assert
        assertNotNull(client);
        assertTrue(client.getActif()); // Valeur par défaut = true
    }
    
    @Test
    void builder_devraitEtrePlusLisible_queConstructeur() {
        // ❌ SANS Builder - Constructeur avec beaucoup de paramètres
        Client clientSansBuilder = new Client(
            "Dupont", 
            "Jean", 
            LocalDate.of(1990, 5, 15),
            "123456789",
            "10 rue de la Paix, 75001 Paris"
        );
        
        // ✅ AVEC Builder - Code plus lisible et explicite
        Client clientAvecBuilder = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("123456789")
                .adresse("10 rue de la Paix, 75001 Paris")
                .build();
        
        // Assert - Les deux produisent le même résultat
        assertEquals(clientSansBuilder.getNom(), clientAvecBuilder.getNom());
        assertEquals(clientSansBuilder.getPrenom(), clientAvecBuilder.getPrenom());
        assertEquals(clientSansBuilder.getDateNaissance(), clientAvecBuilder.getDateNaissance());
    }
    
    @Test
    void builder_devraitPermettre_constructionPartielle() {
        // Le Builder permet de construire des objets même avec des champs manquants
        // (utile pour les tests ou les objets en cours de construction)
        Client client = Client.builder()
                .nom("Test")
                .prenom("User")
                .dateNaissance(LocalDate.of(2000, 1, 1))
                .build();
        
        // Assert - Les champs non définis sont null
        assertNotNull(client);
        assertEquals("Test", client.getNom());
        assertNull(client.getNumeroPermis()); // Non défini via le builder
        assertNull(client.getAdresse()); // Non défini via le builder
    }
    
    @Test
    void builder_devraitPermettre_chainage() {
        // Démontre le chaînage fluide (Fluent Interface)
        Client client = Client.builder()
                .nom("Chainage")
                .prenom("Test")
                .dateNaissance(LocalDate.of(1995, 3, 10))
                .numeroPermis("111111111")
                .adresse("Test Address")
                .actif(false)
                .build();
        
        assertNotNull(client);
        assertFalse(client.getActif());
    }
}
