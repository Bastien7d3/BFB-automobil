package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
import com.BFB.automobile.data.repository.ContratRepository;
import com.BFB.automobile.data.repository.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour VehiculeService
 * 
 * JUSTIFICATION MÉTIER :
 * - Valide la gestion des états de véhicules (DISPONIBLE/LOUE/PANNE)
 * - Teste les règles de location (véhicules en panne = non louables)
 * - Vérifie l'unicité des immatriculations
 * - Rapide : pas de base de données nécessaire
 */
@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {

    @Mock
    private VehiculeRepository vehiculeRepository;
    
    @Mock
    private ContratRepository contratRepository;

    @InjectMocks
    private VehiculeService vehiculeService;

    private Vehicule vehiculeDisponible;

    @BeforeEach
    void setUp() {
        vehiculeDisponible = new Vehicule();
        vehiculeDisponible.setId(1L);
        vehiculeDisponible.setImmatriculation("AB-123-CD");
        vehiculeDisponible.setMarque("Peugeot");
        vehiculeDisponible.setModele("308");
        vehiculeDisponible.setAnnee(2023);
        vehiculeDisponible.setPrixJournalier(new BigDecimal("50.00"));
        vehiculeDisponible.setEtat(EtatVehicule.DISPONIBLE);
    }

    // ==================== TESTS ESSENTIELS ====================

    @Test
    void getAllVehicules_ShouldReturnAllVehicules() {
        // Given
        List<Vehicule> vehicules = Arrays.asList(vehiculeDisponible);
        when(vehiculeRepository.findAll()).thenReturn(vehicules);

        // When
        List<Vehicule> result = vehiculeService.getAllVehicules();

        // Then
        assertEquals(1, result.size());
        assertEquals(vehiculeDisponible, result.get(0));
    }

    @Test
    void getVehiculeById_ExistingVehicule_ShouldReturnVehicule() {
        // Given
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));

        // When
        Vehicule result = vehiculeService.getVehiculeById(1L);

        // Then
        assertEquals(vehiculeDisponible, result);
    }

    @Test
    void getVehiculeById_NonExistingVehicule_ShouldThrowException() {
        // Given
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> vehiculeService.getVehiculeById(99L));
        assertEquals("Véhicule non trouvé avec l'ID : 99", exception.getMessage());
    }

    @Test
    void createVehicule_ValidVehicule_ShouldSaveVehicule() {
        // Given
        when(vehiculeRepository.existsByImmatriculation(anyString())).thenReturn(false);
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeDisponible);

        // When
        Vehicule result = vehiculeService.createVehicule(vehiculeDisponible);

        // Then
        assertNotNull(result);
        assertEquals(EtatVehicule.DISPONIBLE, result.getEtat());
        verify(vehiculeRepository).save(vehiculeDisponible);
    }

    @Test
    void createVehicule_DuplicateImmatriculation_ShouldThrowException() {
        // Given - Immatriculation déjà existante
        when(vehiculeRepository.existsByImmatriculation(anyString())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> vehiculeService.createVehicule(vehiculeDisponible));
        assertTrue(exception.getMessage().contains("immatriculation"));
    }

    @Test
    void getVehiculesDisponibles_ShouldReturnOnlyAvailableVehicules() {
        // Given
        List<Vehicule> vehiculesDisponibles = Arrays.asList(vehiculeDisponible);
        when(vehiculeRepository.findByEtat(EtatVehicule.DISPONIBLE))
            .thenReturn(vehiculesDisponibles);

        // When
        List<Vehicule> result = vehiculeService.getVehiculesDisponibles();

        // Then
        assertEquals(1, result.size());
        assertEquals(EtatVehicule.DISPONIBLE, result.get(0).getEtat());
    }

    @Test
    void changerEtatVehicule_ValidChange_ShouldUpdateEtat() {
        // Given
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehiculeDisponible));
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehiculeDisponible);

        // When
        vehiculeService.changerEtatVehicule(1L, EtatVehicule.PANNE);

        // Then
        verify(vehiculeRepository).save(argThat(vehicule -> 
            vehicule.getEtat() == EtatVehicule.PANNE));
    }

    @Test
    void changerEtatVehicule_NonExistingVehicule_ShouldThrowException() {
        // Given
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> vehiculeService.changerEtatVehicule(99L, EtatVehicule.PANNE));
        assertEquals("Véhicule non trouvé avec l'ID : 99", exception.getMessage());
    }
}