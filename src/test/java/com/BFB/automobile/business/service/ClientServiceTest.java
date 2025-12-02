package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.Client;
import com.BFB.automobile.data.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ClientService
 * Tests isolés avec mocks des dépendances
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService - Tests unitaires")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client clientValide;
    private Client autreClient;

    @BeforeEach
    void setUp() {
        clientValide = new Client();
        clientValide.setId(1L);
        clientValide.setNom("Dupont");
        clientValide.setPrenom("Jean");
        clientValide.setDateNaissance(LocalDate.of(1990, 1, 1));
        clientValide.setNumeroPermis("PERM123456");
        clientValide.setAdresse("123 Rue de la Paix");
        clientValide.setTelephone("0123456789");
        clientValide.setEmail("jean.dupont@email.com");

        autreClient = new Client();
        autreClient.setId(2L);
        autreClient.setNom("Martin");
        autreClient.setPrenom("Sophie");
        autreClient.setDateNaissance(LocalDate.of(1985, 5, 15));
        autreClient.setNumeroPermis("PERM789123");
        autreClient.setAdresse("456 Avenue des Fleurs");
        autreClient.setTelephone("0987654321");
        autreClient.setEmail("sophie.martin@email.com");
    }

    @Test
    @DisplayName("Devrait retourner tous les clients")
    void getAllClients_ShouldReturnAllClients() {
        // Given
        List<Client> clientsAttendus = Arrays.asList(clientValide, autreClient);
        when(clientRepository.findAll()).thenReturn(clientsAttendus);

        // When
        List<Client> result = clientService.getAllClients();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(clientValide));
        assertTrue(result.contains(autreClient));
        verify(clientRepository).findAll();
    }

    @Test
    @DisplayName("Devrait retourner un client par son ID")
    void getClientById_WhenClientExists_ShouldReturnClient() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientValide));

        // When
        Client result = clientService.getClientById(1L);

        // Then
        assertEquals(clientValide, result);
        verify(clientRepository).findById(1L);
    }

    @Test
    @DisplayName("Devrait lever une exception si le client n'existe pas")
    void getClientById_WhenClientNotExists_ShouldThrowException() {
        // Given
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.getClientById(99L));
        
        assertEquals("Client non trouvé avec l'ID : 99", exception.getMessage());
        verify(clientRepository).findById(99L);
    }

    @Test
    @DisplayName("Devrait créer un client valide")
    void createClient_WithValidData_ShouldSaveClient() {
        // Given
        Client nouveauClient = new Client();
        nouveauClient.setNom("Nouveau");
        nouveauClient.setPrenom("Client");
        nouveauClient.setDateNaissance(LocalDate.of(1995, 3, 20));
        nouveauClient.setNumeroPermis("PERM999888");
        nouveauClient.setEmail("nouveau@email.com");

        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any(LocalDate.class)))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(nouveauClient);

        // When
        Client result = clientService.createClient(nouveauClient);

        // Then
        assertNotNull(result);
        verify(clientRepository).save(nouveauClient);
        verify(clientRepository).existsByNomAndPrenomAndDateNaissance("Nouveau", "Client", LocalDate.of(1995, 3, 20));
        verify(clientRepository).existsByNumeroPermis("PERM999888");
    }

    @Test
    @DisplayName("Devrait lever une exception si le client existe déjà")
    void createClient_WhenClientAlreadyExists_ShouldThrowException() {
        // Given
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any(LocalDate.class)))
            .thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.createClient(clientValide));
        
        assertEquals("Un client avec ces informations (nom, prénom, date de naissance) existe déjà", 
            exception.getMessage());
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait lever une exception si le numéro de permis existe déjà")
    void createClient_WhenPermisAlreadyExists_ShouldThrowException() {
        // Given
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any(LocalDate.class)))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.createClient(clientValide));
        
        assertEquals("Ce numéro de permis est déjà utilisé par un autre client", 
            exception.getMessage());
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait mettre à jour un client existant")
    void updateClient_WithValidData_ShouldUpdateClient() {
        // Given
        Client clientModifie = new Client();
        clientModifie.setId(1L);
        clientModifie.setNom("Dupont");
        clientModifie.setPrenom("Jean-Pierre");  // Prénom modifié
        clientModifie.setDateNaissance(LocalDate.of(1990, 1, 1));
        clientModifie.setNumeroPermis("PERM123456");
        clientModifie.setAdresse("789 Nouvelle Adresse");
        clientModifie.setTelephone("0111111111");
        clientModifie.setEmail("jean.pierre@email.com");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientValide));
        when(clientRepository.save(any(Client.class))).thenReturn(clientModifie);

        // When
        Client result = clientService.updateClient(1L, clientModifie);

        // Then
        assertNotNull(result);
        verify(clientRepository).findById(1L);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Devrait supprimer un client existant")
    void deleteClient_WhenClientExists_ShouldDeleteClient() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientValide));
        doNothing().when(clientRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> clientService.deleteClient(1L));

        // Then
        verify(clientRepository).findById(1L);
        verify(clientRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Devrait lever une exception lors de la suppression d'un client inexistant")
    void deleteClient_WhenClientNotExists_ShouldThrowException() {
        // Given
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.deleteClient(99L));
        
        assertEquals("Client non trouvé avec l'ID : 99", exception.getMessage());
        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Devrait gérer les exceptions de contrainte d'intégrité")
    void createClient_WhenDataIntegrityViolation_ShouldThrowBusinessException() {
        // Given
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any(LocalDate.class)))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(false);
        when(clientRepository.save(any(Client.class)))
            .thenThrow(new DataIntegrityViolationException("Contrainte violée"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.createClient(clientValide));
        
        assertTrue(exception.getMessage().contains("Erreur lors de la création du client"));
    }
}