package com.BFB.automobile.data.repository;

import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour VehiculeRepository
 * 
 * JUSTIFICATION COUCHE DATA :
 * - Teste les requêtes par état de véhicule
 * - Valide l'unicité des immatriculations
 * - Vérifie les contraintes de base de données
 */
@DataJpaTest
@DisplayName("VehiculeRepository - Tests d'intégration")
class VehiculeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VehiculeRepository vehiculeRepository;

    private Vehicule vehiculeDisponible;
    private Vehicule vehiculeLoue;
    private Vehicule vehiculeEnPanne;

    @BeforeEach
    void setUp() {
        vehiculeDisponible = new Vehicule();
        vehiculeDisponible.setImmatriculation("AB-123-CD");
        vehiculeDisponible.setMarque("Peugeot");
        vehiculeDisponible.setModele("308");
        vehiculeDisponible.setAnnee(2023);
        vehiculeDisponible.setPrixJournalier(new BigDecimal("50.00"));
        vehiculeDisponible.setEtat(EtatVehicule.DISPONIBLE);

        vehiculeLoue = new Vehicule();
        vehiculeLoue.setImmatriculation("EF-456-GH");
        vehiculeLoue.setMarque("Renault");
        vehiculeLoue.setModele("Clio");
        vehiculeLoue.setAnnee(2022);
        vehiculeLoue.setPrixJournalier(new BigDecimal("45.00"));
        vehiculeLoue.setEtat(EtatVehicule.LOUE);

        vehiculeEnPanne = new Vehicule();
        vehiculeEnPanne.setImmatriculation("IJ-789-KL");
        vehiculeEnPanne.setMarque("Citroën");
        vehiculeEnPanne.setModele("C3");
        vehiculeEnPanne.setAnnee(2021);
        vehiculeEnPanne.setPrixJournalier(new BigDecimal("40.00"));
        vehiculeEnPanne.setEtat(EtatVehicule.PANNE);
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer un véhicule")
    void saveAndFindById_ShouldWork() {
        // When
        Vehicule savedVehicule = vehiculeRepository.save(vehiculeDisponible);
        Vehicule foundVehicule = vehiculeRepository.findById(savedVehicule.getId()).orElse(null);

        // Then
        assertThat(foundVehicule).isNotNull();
        assertThat(foundVehicule.getImmatriculation()).isEqualTo("AB-123-CD");
        assertThat(foundVehicule.getMarque()).isEqualTo("Peugeot");
        assertThat(foundVehicule.getEtat()).isEqualTo(EtatVehicule.DISPONIBLE);
    }

    @Test
    @DisplayName("Devrait retourner les véhicules par état")
    void findByEtat_ShouldReturnVehiclesWithSpecificState() {
        // Given
        entityManager.persistAndFlush(vehiculeDisponible);
        entityManager.persistAndFlush(vehiculeLoue);
        entityManager.persistAndFlush(vehiculeEnPanne);

        // When
        List<Vehicule> vehiculesDisponibles = vehiculeRepository.findByEtat(EtatVehicule.DISPONIBLE);
        List<Vehicule> vehiculesLoues = vehiculeRepository.findByEtat(EtatVehicule.LOUE);
        List<Vehicule> vehiculesEnPanne = vehiculeRepository.findByEtat(EtatVehicule.PANNE);

        // Then
        assertThat(vehiculesDisponibles).hasSize(1);
        assertThat(vehiculesDisponibles.get(0).getImmatriculation()).isEqualTo("AB-123-CD");

        assertThat(vehiculesLoues).hasSize(1);
        assertThat(vehiculesLoues.get(0).getImmatriculation()).isEqualTo("EF-456-GH");

        assertThat(vehiculesEnPanne).hasSize(1);
        assertThat(vehiculesEnPanne.get(0).getImmatriculation()).isEqualTo("IJ-789-KL");
    }

    @Test
    @DisplayName("Devrait détecter une immatriculation existante")
    void existsByImmatriculation_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(vehiculeDisponible);

        // When
        boolean exists = vehiculeRepository.existsByImmatriculation("AB-123-CD");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Devrait retourner false pour une immatriculation inexistante")
    void existsByImmatriculation_ShouldReturnFalse() {
        // When
        boolean exists = vehiculeRepository.existsByImmatriculation("XX-999-YY");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Devrait retourner tous les véhicules")
    void findAll_ShouldReturnAllVehicules() {
        // Given
        entityManager.persistAndFlush(vehiculeDisponible);
        entityManager.persistAndFlush(vehiculeLoue);

        // When
        List<Vehicule> vehicules = vehiculeRepository.findAll();

        // Then
        assertThat(vehicules).hasSize(2);
        assertThat(vehicules).extracting(Vehicule::getMarque)
            .containsExactlyInAnyOrder("Peugeot", "Renault");
    }

    @Test
    @DisplayName("Devrait filtrer les véhicules par marque")
    void findByMarque_ShouldReturnVehiclesOfSpecificBrand() {
        // Given
        entityManager.persistAndFlush(vehiculeDisponible);
        entityManager.persistAndFlush(vehiculeLoue);

        // When
        List<Vehicule> peugeots = vehiculeRepository.findByMarque("Peugeot");

        // Then
        assertThat(peugeots).hasSize(1);
        assertThat(peugeots.get(0).getModele()).isEqualTo("308");
    }
}