package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.business.service.ClientService;
import com.BFB.automobile.data.Client;
import com.BFB.automobile.presentation.dto.ClientDTO;
import com.BFB.automobile.presentation.mapper.ClientMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du contrôleur ClientController avec MockMvc
 * Utilise @WebMvcTest pour tester uniquement la couche web
 */
@WebMvcTest(ClientController.class)
class ClientControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ClientService clientService;
    
    @MockBean
    private ClientMapper clientMapper;
    
    private Client client;
    private ClientDTO clientDTO;
    
    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setNom("Dupont");
        client.setPrenom("Jean");
        client.setDateNaissance(LocalDate.of(1990, 5, 15));
        client.setNumeroPermis("123456789");
        client.setAdresse("10 Rue de la Paix, 75001 Paris");
        client.setActif(true);
        
        clientDTO = new ClientDTO();
        clientDTO.setId(1L);
        clientDTO.setNom("Dupont");
        clientDTO.setPrenom("Jean");
        clientDTO.setDateNaissance(LocalDate.of(1990, 5, 15));
        clientDTO.setNumeroPermis("123456789");
        clientDTO.setAdresse("10 Rue de la Paix, 75001 Paris");
        clientDTO.setActif(true);
    }
    
    // ========== Tests GET /api/clients ==========
    
    @Test
    void obtenirTousLesClients_devraitRetourner200AvecListeClients() throws Exception {
        // Arrange
        List<Client> clients = Arrays.asList(client);
        List<ClientDTO> clientDTOs = Arrays.asList(clientDTO);
        
        when(clientService.obtenirTousLesClients()).thenReturn(clients);
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/clients"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].nom").value("Dupont"))
            .andExpect(jsonPath("$[0].prenom").value("Jean"));
        
        verify(clientService, times(1)).obtenirTousLesClients();
    }
    
    @Test
    void obtenirClientsActifs_devraitRetourner200AvecSeulementActifs() throws Exception {
        // Arrange
        List<Client> clients = Arrays.asList(client);
        when(clientService.obtenirTousLesClientsActifs()).thenReturn(clients);
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/clients?actif=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].actif").value(true));
        
        verify(clientService, times(1)).obtenirTousLesClientsActifs();
        verify(clientService, never()).obtenirTousLesClients();
    }
    
    // ========== Tests GET /api/clients/{id} ==========
    
    @Test
    void obtenirClientParId_devraitRetourner200AvecClient() throws Exception {
        // Arrange
        when(clientService.obtenirClientParId(1L)).thenReturn(client);
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/clients/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nom").value("Dupont"))
            .andExpect(jsonPath("$.prenom").value("Jean"));
        
        verify(clientService, times(1)).obtenirClientParId(1L);
    }
    
    @Test
    void obtenirClientParId_devraitRetourner404SiClientNonTrouve() throws Exception {
        // Arrange
        when(clientService.obtenirClientParId(999L))
            .thenThrow(new BusinessException("CLIENT_NON_TROUVE", "Client non trouvé"));
        
        // Act & Assert
        mockMvc.perform(get("/api/clients/999"))
            .andExpect(status().isBadRequest());
        
        verify(clientService, times(1)).obtenirClientParId(999L);
    }
    
    // ========== Tests POST /api/clients ==========
    
    @Test
    void creerClient_devraitRetourner201AvecClient() throws Exception {
        // Arrange
        when(clientMapper.toEntity(any(ClientDTO.class))).thenReturn(client);
        when(clientService.creerClient(any(Client.class))).thenReturn(client);
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);
        
        // Act & Assert
        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nom").value("Dupont"))
            .andExpect(jsonPath("$.prenom").value("Jean"));
        
        verify(clientService, times(1)).creerClient(any(Client.class));
    }
    
    @Test
    void creerClient_devraitRetourner400SiClientExiste() throws Exception {
        // Arrange
        when(clientMapper.toEntity(any(ClientDTO.class))).thenReturn(client);
        when(clientService.creerClient(any(Client.class)))
            .thenThrow(new BusinessException("CLIENT_EXISTE_DEJA", "Client existe déjà"));
        
        // Act & Assert
        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
            .andExpect(status().isBadRequest());
        
        verify(clientService, times(1)).creerClient(any(Client.class));
    }
    
    // ========== Tests PUT /api/clients/{id} ==========
    
    @Test
    void mettreAJourClient_devraitRetourner200AvecClientModifie() throws Exception {
        // Arrange
        when(clientMapper.toEntity(any(ClientDTO.class))).thenReturn(client);
        when(clientService.mettreAJourClient(anyLong(), any(Client.class))).thenReturn(client);
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);
        
        // Act & Assert
        mockMvc.perform(put("/api/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nom").value("Dupont"));
        
        verify(clientService, times(1)).mettreAJourClient(eq(1L), any(Client.class));
    }
    
    @Test
    void mettreAJourClient_devraitRetourner404SiClientNonTrouve() throws Exception {
        // Arrange
        when(clientMapper.toEntity(any(ClientDTO.class))).thenReturn(client);
        when(clientService.mettreAJourClient(anyLong(), any(Client.class)))
            .thenThrow(new BusinessException("CLIENT_NON_TROUVE", "Client non trouvé"));
        
        // Act & Assert
        mockMvc.perform(put("/api/clients/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientDTO)))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Tests DELETE /api/clients/{id} ==========
    
    @Test
    void desactiverClient_devraitRetourner204() throws Exception {
        // Arrange
        doNothing().when(clientService).desactiverClient(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/clients/1"))
            .andExpect(status().isNoContent());
        
        verify(clientService, times(1)).desactiverClient(1L);
    }
    
    @Test
    void desactiverClient_devraitRetourner404SiClientNonTrouve() throws Exception {
        // Arrange
        doThrow(new BusinessException("CLIENT_NON_TROUVE", "Client non trouvé"))
            .when(clientService).desactiverClient(999L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/clients/999"))
            .andExpect(status().isBadRequest());
    }
    
    // ========== Tests GET /api/clients/permis/{numeroPermis} ==========
    
    @Test
    void rechercherParPermis_devraitRetourner200AvecClient() throws Exception {
        // Arrange
        when(clientService.rechercherParNumeroPermis("123456789")).thenReturn(java.util.Optional.of(client));
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/clients/permis/123456789"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.numeroPermis").value("123456789"));
        
        verify(clientService, times(1)).rechercherParNumeroPermis("123456789");
    }
    
    @Test
    void rechercherParPermis_devraitRetourner404SiNonTrouve() throws Exception {
        // Arrange
        when(clientService.rechercherParNumeroPermis("000000000"))
            .thenReturn(java.util.Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/clients/permis/000000000"))
            .andExpect(status().isNotFound());
    }
}
