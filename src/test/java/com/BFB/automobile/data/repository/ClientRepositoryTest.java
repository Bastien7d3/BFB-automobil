package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration Repository - Requêtes custom uniquement
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
@Transactional
class ClientRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ClientRepository clientRepository;
    
    private Client client1;
    private Client client2;
    
    @BeforeEach
    void setUp() {
        client1 = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("111111111")
                .adresse("10 Rue de la Paix, 75001 Paris")
                .actif(true)
                .build();
        
        client2 = Client.builder()
                .nom("Martin")
                .prenom("Sophie")
                .dateNaissance(LocalDate.of(1985, 8, 20))
                .numeroPermis("222222222")
                .adresse("25 Avenue des Champs, 69000 Lyon")
                .actif(true)
                .build();
    }
    
    @Test
    void findByNumeroPermis_devraitRetournerClient() {
        // Arrange
        entityManager.persist(client1);
        entityManager.flush();
        
        // Act
        Optional<Client> result = clientRepository.findByNumeroPermis("111111111");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Dupont", result.get().getNom());
    }
    
    @Test
    void searchByNomAndPrenom_devraitTrouverParRecherche() {
        // Arrange
        entityManager.persist(client1);
        entityManager.persist(client2);
        entityManager.flush();
        
        // Act
        List<Client> results = clientRepository.searchByNomAndPrenom("dupont", "jean");
        
        // Assert
        assertEquals(1, results.size());
        assertEquals("Dupont", results.get(0).getNom());
    }
    
    @Test
    void findByActifTrue_devraitRetournerSeulementActifs() {
        // Arrange
        client2.setActif(false);
        entityManager.persist(client1);
        entityManager.persist(client2);
        entityManager.flush();
        
        // Act
        List<Client> actifs = clientRepository.findByActifTrue();
        
        // Assert
        assertEquals(1, actifs.size());
        assertEquals("Dupont", actifs.get(0).getNom());
    }
}
