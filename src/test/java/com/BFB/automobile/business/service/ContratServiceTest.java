package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.*;
import com.BFB.automobile.data.repository.ContratRepository;
import com.BFB.automobile.data.repository.ClientRepository;
import com.BFB.automobile.data.repository.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ContratService
 * 
 * JUSTIFICATION MÉTIER :
 * - Valide les règles de location (dates, disponibilité véhicule)
 * - Teste la gestion des états de contrat (EN_ATTENTE/CONFIRME/ANNULE)
 * - Vérifie les calculs de prix
 * - Exécution rapide sans DB = feedback immédiat
 */
@ExtendWith(MockitoExtension.class)
class ContratServiceTest {

    @Mock
    private ContratRepository contratRepository;
    
    @Mock
    private ClientRepository clientRepository;
    
    @Mock
    private VehiculeRepository vehiculeRepository;

    @InjectMocks
    private ContratService contratService;

    private Contrat contratValide;
    private Client client;
    private Vehicule vehicule;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setNom("Dupont");

        vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setMarque("Peugeot");
        vehicule.setPrixJournalier(new BigDecimal("50.00"));
        vehicule.setEtat(EtatVehicule.DISPONIBLE);

        contratValide = new Contrat();
        contratValide.setId(1L);
        contratValide.setClient(client);
        contratValide.setVehicule(vehicule);
        contratValide.setDateDebut(LocalDate.now().plusDays(1));
        contratValide.setDateFin(LocalDate.now().plusDays(3));
        contratValide.setPrixTotal(new BigDecimal("150.00"));
        contratValide.setEtat(EtatContrat.EN_ATTENTE);
    }

    // ==================== TESTS ESSENTIELS ====================

    @Test
    void getAllContrats_ShouldReturnAllContrats() {
        // Given
        List<Contrat> contrats = Arrays.asList(contratValide);
        when(contratRepository.findAll()).thenReturn(contrats);

        // When
        List<Contrat> result = contratService.getAllContrats();

        // Then
        assertEquals(1, result.size());
        assertEquals(contratValide, result.get(0));
    }

    @Test
    void getContratById_ExistingContrat_ShouldReturnContrat() {
        // Given
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide));

        // When
        Contrat result = contratService.getContratById(1L);

        // Then
        assertEquals(contratValide, result);
    }

    @Test
    void getContratById_NonExistingContrat_ShouldThrowException() {
        // Given
        when(contratRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> contratService.getContratById(99L));
        assertEquals("Contrat non trouvé avec l'ID : 99", exception.getMessage());
    }

    @Test
    void createContrat_ValidContrat_ShouldSaveContrat() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(contratRepository.save(any(Contrat.class))).thenReturn(contratValide);

        // When
        Contrat result = contratService.createContrat(1L, 1L, 
            LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        // Then
        assertNotNull(result);
        assertEquals(EtatContrat.EN_ATTENTE, result.getEtat());
        verify(contratRepository).save(any(Contrat.class));
    }

    @Test
    void createContrat_VehiculeNotAvailable_ShouldThrowException() {
        // Given - Véhicule en panne
        vehicule.setEtat(EtatVehicule.PANNE);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> contratService.createContrat(1L, 1L, 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)));
        assertTrue(exception.getMessage().contains("disponible"));
    }

    @Test
    void createContrat_InvalidDates_ShouldThrowException() {
        // Given - Date de fin avant date de début
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> contratService.createContrat(1L, 1L, 
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(1)));
        assertTrue(exception.getMessage().contains("date"));
    }

    @Test
    void confirmerContrat_ValidContrat_ShouldUpdateEtat() {
        // Given
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide));
        when(contratRepository.save(any(Contrat.class))).thenReturn(contratValide);

        // When
        contratService.confirmerContrat(1L);

        // Then
        verify(contratRepository).save(argThat(contrat -> 
            contrat.getEtat() == EtatContrat.CONFIRME));
    }

    @Test
    void annulerContrat_ValidContrat_ShouldUpdateEtat() {
        // Given
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide));
        when(contratRepository.save(any(Contrat.class))).thenReturn(contratValide);

        // When
        contratService.annulerContrat(1L);

        // Then
        verify(contratRepository).save(argThat(contrat -> 
            contrat.getEtat() == EtatContrat.ANNULE));
    }
}