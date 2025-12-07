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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour ClientService
 * Couvre toutes les règles métier :
 * - Unicité par (nom, prénom, date de naissance)
 * - Unicité du numéro de permis
 * - Âge minimum de 18 ans
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
        clientValide = Client.builder()
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1990, 5, 15))
                .numeroPermis("123456789")
                .adresse("10 rue de la Paix, 75001 Paris")
                .actif(true)
                .build();
    }
    
    // ========== Tests de création de client ==========
    
    @Test
    void creerClient_devraitReussir_avecDonneesValides() {
        // Arrange
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any(LocalDate.class)))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(clientValide);
        
        // Act
        Client resultat = clientService.creerClient(clientValide);
        
        // Assert
        assertNotNull(resultat);
        assertEquals("Dupont", resultat.getNom());
        verify(clientRepository, times(1)).save(clientValide);
    }
    
    @Test
    void creerClient_devraitLeverException_siClientExisteDeja() {
        // Arrange
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(
            clientValide.getNom(), 
            clientValide.getPrenom(), 
            clientValide.getDateNaissance()))
            .thenReturn(true);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.creerClient(clientValide));
        
        assertEquals("CLIENT_EXISTE_DEJA", exception.getCode());
        assertTrue(exception.getMessage().contains("existe déjà"));
        verify(clientRepository, never()).save(any());
    }
    
    @Test
    void creerClient_devraitLeverException_siNumeroPermisExiste() {
        // Arrange
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any()))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(clientValide.getNumeroPermis()))
            .thenReturn(true);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.creerClient(clientValide));
        
        assertEquals("NUMERO_PERMIS_EXISTE", exception.getCode());
        verify(clientRepository, never()).save(any());
    }
    
    @Test
    void creerClient_devraitLeverException_siClientMoins18Ans() {
        // Arrange
        clientValide.setDateNaissance(LocalDate.now().minusYears(17)); // 17 ans
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any()))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(false);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.creerClient(clientValide));
        
        assertEquals("AGE_INSUFFISANT", exception.getCode());
        assertTrue(exception.getMessage().contains("18 ans"));
        verify(clientRepository, never()).save(any());
    }
    
    @Test
    void creerClient_devraitAccepter_siClientExactement18Ans() {
        // Arrange
        clientValide.setDateNaissance(LocalDate.now().minusYears(18)); // Exactement 18 ans
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any()))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(clientValide);
        
        // Act
        Client resultat = clientService.creerClient(clientValide);
        
        // Assert
        assertNotNull(resultat);
        verify(clientRepository, times(1)).save(clientValide);
    }
    
    // ========== Tests de mise à jour ==========
    
    @Test
    void mettreAJourClient_devraitReussir_avecDonneesValides() {
        // Arrange
        Client clientExistant = new Client();
        clientExistant.setId(1L);
        clientExistant.setNumeroPermis("123456789");
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientExistant));
        when(clientRepository.save(any(Client.class))).thenReturn(clientExistant);
        
        Client modifications = new Client();
        modifications.setNom("Martin");
        modifications.setPrenom("Pierre");
        modifications.setDateNaissance(LocalDate.of(1985, 3, 20));
        modifications.setNumeroPermis("123456789"); // Même numéro
        modifications.setAdresse("20 avenue des Champs");
        
        // Act
        Client resultat = clientService.mettreAJourClient(1L, modifications);
        
        // Assert
        assertNotNull(resultat);
        assertEquals("Martin", resultat.getNom());
        assertEquals("Pierre", resultat.getPrenom());
        verify(clientRepository, times(1)).save(clientExistant);
    }
    
    @Test
    void mettreAJourClient_devraitLeverException_siClientNonTrouve() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.mettreAJourClient(999L, clientValide));
        
        assertEquals("CLIENT_NON_TROUVE", exception.getCode());
        verify(clientRepository, never()).save(any());
    }
    
    @Test
    void mettreAJourClient_devraitLeverException_siNouveauNumeroPermisExiste() {
        // Arrange
        Client clientExistant = new Client();
        clientExistant.setId(1L);
        clientExistant.setNumeroPermis("123456789");
        
        Client autreClient = new Client();
        autreClient.setId(2L);
        autreClient.setNumeroPermis("987654321");
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientExistant));
        when(clientRepository.findByNumeroPermis("987654321")).thenReturn(Optional.of(autreClient));
        
        Client modifications = new Client();
        modifications.setNumeroPermis("987654321"); // Numéro déjà utilisé par autre client
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.mettreAJourClient(1L, modifications));
        
        assertEquals("NUMERO_PERMIS_EXISTE", exception.getCode());
        verify(clientRepository, never()).save(any());
    }
    
    // ========== Tests de désactivation ==========
    
    @Test
    void desactiverClient_devraitReussir() {
        // Arrange
        Client client = new Client();
        client.setId(1L);
        client.setActif(true);
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        
        // Act
        clientService.desactiverClient(1L);
        
        // Assert
        assertFalse(client.getActif());
        verify(clientRepository, times(1)).save(client);
    }
    
    @Test
    void desactiverClient_devraitLeverException_siClientNonTrouve() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(BusinessException.class,
            () -> clientService.desactiverClient(999L));
    }
    
    // ========== Tests de récupération ==========
    
    @Test
    void obtenirTousLesClients_devraitRetournerListeCompleteClients() {
        // Arrange
        List<Client> clients = Arrays.asList(new Client(), new Client());
        when(clientRepository.findAll()).thenReturn(clients);
        
        // Act
        List<Client> resultat = clientService.obtenirTousLesClients();
        
        // Assert
        assertEquals(2, resultat.size());
        verify(clientRepository, times(1)).findAll();
    }
    
    @Test
    void obtenirTousLesClientsActifs_devraitRetournerSeulementClientsActifs() {
        // Arrange
        Client client1 = new Client();
        client1.setActif(true);
        
        List<Client> clientsActifs = Arrays.asList(client1);
        when(clientRepository.findByActifTrue()).thenReturn(clientsActifs);
        
        // Act
        List<Client> resultat = clientService.obtenirTousLesClientsActifs();
        
        // Assert
        assertEquals(1, resultat.size());
        assertTrue(resultat.get(0).getActif());
        verify(clientRepository, times(1)).findByActifTrue();
    }
    
    @Test
    void obtenirClientParId_devraitRetournerClient_siExiste() {
        // Arrange
        Client client = new Client();
        client.setId(1L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        
        // Act
        Client resultat = clientService.obtenirClientParId(1L);
        
        // Assert
        assertNotNull(resultat);
        assertEquals(1L, resultat.getId());
    }
    
    @Test
    void obtenirClientParId_devraitLeverException_siClientNonTrouve() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.obtenirClientParId(999L));
        
        assertEquals("CLIENT_NON_TROUVE", exception.getCode());
    }
    
    // ========== Tests de recherche ==========
    
    @Test
    void rechercherClients_devraitRechercher_parNomEtPrenom() {
        // Arrange
        List<Client> clients = Arrays.asList(new Client());
        when(clientRepository.searchByNomAndPrenom("Dupont", "Jean")).thenReturn(clients);
        
        // Act
        List<Client> resultat = clientService.rechercherClients("Dupont", "Jean");
        
        // Assert
        assertEquals(1, resultat.size());
        verify(clientRepository, times(1)).searchByNomAndPrenom("Dupont", "Jean");
    }
    
    @Test
    void rechercherClients_devraitRechercher_parNomSeul() {
        // Arrange
        List<Client> clients = Arrays.asList(new Client());
        when(clientRepository.findByNomContainingIgnoreCase("Dupont")).thenReturn(clients);
        
        // Act
        List<Client> resultat = clientService.rechercherClients("Dupont", null);
        
        // Assert
        assertEquals(1, resultat.size());
        verify(clientRepository, times(1)).findByNomContainingIgnoreCase("Dupont");
    }
    
    @Test
    void rechercherClients_devraitRechercher_parPrenomSeul() {
        // Arrange
        List<Client> clients = Arrays.asList(new Client());
        when(clientRepository.findByPrenomContainingIgnoreCase("Jean")).thenReturn(clients);
        
        // Act
        List<Client> resultat = clientService.rechercherClients(null, "Jean");
        
        // Assert
        assertEquals(1, resultat.size());
        verify(clientRepository, times(1)).findByPrenomContainingIgnoreCase("Jean");
    }
    
    @Test
    void rechercherClients_devraitRetournerTous_siAucunCritere() {
        // Arrange
        List<Client> clients = Arrays.asList(new Client(), new Client());
        when(clientRepository.findAll()).thenReturn(clients);
        
        // Act
        List<Client> resultat = clientService.rechercherClients(null, null);
        
        // Assert
        assertEquals(2, resultat.size());
        verify(clientRepository, times(1)).findAll();
    }
    
    @Test
    void rechercherParNumeroPermis_devraitRetournerClient_siTrouve() {
        // Arrange
        Client client = new Client();
        client.setNumeroPermis("123456789");
        when(clientRepository.findByNumeroPermis("123456789")).thenReturn(Optional.of(client));
        
        // Act
        Optional<Client> resultat = clientService.rechercherParNumeroPermis("123456789");
        
        // Assert
        assertTrue(resultat.isPresent());
        assertEquals("123456789", resultat.get().getNumeroPermis());
    }
    
    @Test
    void rechercherParNumeroPermis_devraitRetournerOptionalVide_siNonTrouve() {
        // Arrange
        when(clientRepository.findByNumeroPermis("999999999")).thenReturn(Optional.empty());
        
        // Act
        Optional<Client> resultat = clientService.rechercherParNumeroPermis("999999999");
        
        // Assert
        assertFalse(resultat.isPresent());
    }
}
