package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour ClientRepository
 * 
 * JUSTIFICATION COUCHE DATA :
 * - Valide les requêtes JPA personnalisées
 * - Teste les contraintes de base de données
 * - Vérifie l'intégrité des données
 * - Tests avec vraie base H2 en mémoire
 */
@DataJpaTest
@DisplayName("ClientRepository - Tests d'intégration")
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    private Client client1;
    private Client client2;

    @BeforeEach
    void setUp() {
        client1 = new Client();
        client1.setNom("Dupont");
        client1.setPrenom("Jean");
        client1.setDateNaissance(LocalDate.of(1990, 1, 1));
        client1.setNumeroPermis("PERM123456");
        client1.setAdresse("123 Rue de la Paix");
        client1.setTelephone("0123456789");
        client1.setEmail("jean.dupont@email.com");

        client2 = new Client();
        client2.setNom("Martin");
        client2.setPrenom("Sophie");
        client2.setDateNaissance(LocalDate.of(1985, 5, 15));
        client2.setNumeroPermis("PERM789123");
        client2.setAdresse("456 Avenue des Fleurs");
        client2.setTelephone("0987654321");
        client2.setEmail("sophie.martin@email.com");
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer un client")
    void saveAndFindById_ShouldWork() {
        // When
        Client savedClient = clientRepository.save(client1);
        Client foundClient = clientRepository.findById(savedClient.getId()).orElse(null);

        // Then
        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getNom()).isEqualTo("Dupont");
        assertThat(foundClient.getPrenom()).isEqualTo("Jean");
        assertThat(foundClient.getNumeroPermis()).isEqualTo("PERM123456");
    }

    @Test
    @DisplayName("Devrait détecter un client existant par nom, prénom et date de naissance")
    void existsByNomAndPrenomAndDateNaissance_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(client1);

        // When
        boolean exists = clientRepository.existsByNomAndPrenomAndDateNaissance(
            "Dupont", "Jean", LocalDate.of(1990, 1, 1));

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Devrait retourner false pour un client inexistant")
    void existsByNomAndPrenomAndDateNaissance_ShouldReturnFalse() {
        // When
        boolean exists = clientRepository.existsByNomAndPrenomAndDateNaissance(
            "Inexistant", "Client", LocalDate.of(2000, 1, 1));

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Devrait détecter un numéro de permis existant")
    void existsByNumeroPermis_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(client1);

        // When
        boolean exists = clientRepository.existsByNumeroPermis("PERM123456");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Devrait retourner false pour un numéro de permis inexistant")
    void existsByNumeroPermis_ShouldReturnFalse() {
        // When
        boolean exists = clientRepository.existsByNumeroPermis("PERMXXXX");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Devrait retourner tous les clients")
    void findAll_ShouldReturnAllClients() {
        // Given
        entityManager.persistAndFlush(client1);
        entityManager.persistAndFlush(client2);

        // When
        List<Client> clients = clientRepository.findAll();

        // Then
        assertThat(clients).hasSize(2);
        assertThat(clients).extracting(Client::getNom)
            .containsExactlyInAnyOrder("Dupont", "Martin");
    }

    @Test
    @DisplayName("Devrait supprimer un client")
    void deleteById_ShouldRemoveClient() {
        // Given
        Client savedClient = entityManager.persistAndFlush(client1);
        Long clientId = savedClient.getId();

        // When
        clientRepository.deleteById(clientId);
        entityManager.flush();

        // Then
        assertThat(clientRepository.findById(clientId)).isEmpty();
    }
}