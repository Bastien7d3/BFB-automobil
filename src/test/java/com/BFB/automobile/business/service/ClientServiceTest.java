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
    
    @Test
    void creerClient_devraitReussir_avecDonneesValides() {
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any(LocalDate.class)))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(clientValide);
        
        Client resultat = clientService.creerClient(clientValide);
        
        assertNotNull(resultat);
        verify(clientRepository, times(1)).save(clientValide);
    }
    
    @Test
    void creerClient_devraitLeverException_siClientExisteDeja() {
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(
            clientValide.getNom(), 
            clientValide.getPrenom(), 
            clientValide.getDateNaissance()))
            .thenReturn(true);
        
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> clientService.creerClient(clientValide));
        
        assertEquals("CLIENT_EXISTE_DEJA", exception.getCode());
    }
    
    @Test
    void creerClient_devraitLeverException_siNumeroPermisExiste() {
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any()))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(clientValide.getNumeroPermis()))
            .thenReturn(true);
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.creerClient(clientValide));
        
        assertEquals("NUMERO_PERMIS_EXISTE", exception.getCode());
    }
    
    @Test
    void creerClient_devraitLeverException_siClientMoins18Ans() {
        clientValide.setDateNaissance(LocalDate.now().minusYears(17));
        when(clientRepository.existsByNomAndPrenomAndDateNaissance(anyString(), anyString(), any()))
            .thenReturn(false);
        when(clientRepository.existsByNumeroPermis(anyString())).thenReturn(false);
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.creerClient(clientValide));
        
        assertEquals("AGE_INSUFFISANT", exception.getCode());
    }
    
    @Test
    void mettreAJourClient_devraitReussir_avecDonneesValides() {
        Client clientExistant = new Client();
        clientExistant.setId(1L);
        clientExistant.setNumeroPermis("123456789");
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientExistant));
        when(clientRepository.save(any(Client.class))).thenReturn(clientExistant);
        
        Client modifications = new Client();
        modifications.setNom("Martin");
        modifications.setNumeroPermis("123456789");
        
        Client resultat = clientService.mettreAJourClient(1L, modifications);
        
        assertNotNull(resultat);
        verify(clientRepository, times(1)).save(clientExistant);
    }
    
    @Test
    void mettreAJourClient_devraitLeverException_siClientNonTrouve() {
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> clientService.mettreAJourClient(999L, clientValide));
        
        assertEquals("CLIENT_NON_TROUVE", exception.getCode());
    }
    
    @Test
    void desactiverClient_devraitMarquerCommeInactif() {
        Client client = new Client();
        client.setId(1L);
        client.setActif(true);
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        
        clientService.desactiverClient(1L);
        
        assertFalse(client.getActif());
        verify(clientRepository, times(1)).save(client);
    }
    
    @Test
    void rechercherClients_devraitRetournerResultatsRecherche() {
        when(clientRepository.searchByNomAndPrenom("Dupont", "Jean"))
            .thenReturn(Arrays.asList(clientValide));
        
        List<Client> results = clientService.rechercherClients("Dupont", "Jean");
        
        assertEquals(1, results.size());
        verify(clientRepository, times(1)).searchByNomAndPrenom("Dupont", "Jean");
    }
}
