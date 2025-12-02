package com.BFB.automobile.business.service;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.data.Client;
import com.BFB.automobile.data.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ClientService
 * 
 * POURQUOI CES TESTS ?
 * - Valident la logique métier critique (unicité client, validation permis)
 * - Exécution rapide (pas de DB)
 * - Détectent les régressions immédiatement
 * - Documentent le comportement attendu
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client clientValide;

    @BeforeEach
    void setUp() {
        clientValide = new Client();
        clientValide.setId(1L);
        clientValide.setNom("Dupont");
        clientValide.setPrenom("Jean");
        clientValide.setDateNaissance(LocalDate.of(1990, 1, 1));
        clientValide.setNumeroPermis("PERM123456");
        clientValide.setEmail("jean.dupont@email.com");
    }

    // ==================== TESTS ESSENTIELS ====================

    @Test
    void getAllClients_ShouldReturnAllClients() {
        // Given
        List<Client> clients = Arrays.asList(clientValide);
        when(clientRepository.findAll()).thenReturn(clients);

        // When
        List<Client> result = clientService.getAllClients();

        // Then
        assertEquals(1, result.size());
        assertEquals(clientValide, result.get(0));
    }

    @Test
    void getClientById_ExistingClient_ShouldReturnClient() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientValide));

        // When
        Client result = clientService.getClientById(1L);

        // Then
        assertEquals(clientValide, result);
    }

    @Test
    void getClientById_NonExistingClient_ShouldThrowException() {
        // Given
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.getClientById(99L));
        assertEquals("Client non trouvé avec l'ID : 99", exception.getMessage());
    }

    @Test
    void createClient_ValidClient_ShouldSaveClient() {
        // Given
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any()))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(clientValide);

        // When
        Client result = clientService.createClient(clientValide);

        // Then
        assertNotNull(result);
        verify(clientRepository).save(clientValide);
    }

    @Test
    void createClient_DuplicateClient_ShouldThrowException() {
        // Given - Client déjà existant
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any()))
            .thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.createClient(clientValide));
        assertTrue(exception.getMessage().contains("existe déjà"));
    }

    @Test
    void createClient_DuplicatePermis_ShouldThrowException() {
        // Given - Permis déjà utilisé
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any()))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.createClient(clientValide));
        assertTrue(exception.getMessage().contains("permis"));
    }
}