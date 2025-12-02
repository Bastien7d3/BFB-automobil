package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.business.service.ClientService;
import com.BFB.automobile.data.Client;
import com.BFB.automobile.presentation.dto.ClientDTO;
import com.BFB.automobile.presentation.mapper.ClientMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour ClientController
 * 
 * JUSTIFICATION COUCHE PRESENTATION :
 * - Teste les endpoints REST (GET, POST, PUT, DELETE)
 * - Valide la sérialisation/désérialisation JSON
 * - Vérifie les codes de statut HTTP
 * - Teste la gestion des erreurs (404, 400, 500)
 */
@WebMvcTest(ClientController.class)
@DisplayName("ClientController - Tests d'intégration")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private ClientMapper clientMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Client client;
    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setNom("Dupont");
        client.setPrenom("Jean");
        client.setDateNaissance(LocalDate.of(1990, 1, 1));
        client.setNumeroPermis("PERM123456");
        client.setAdresse("123 Rue de la Paix");
        client.setTelephone("0123456789");
        client.setEmail("jean.dupont@email.com");

        clientDTO = new ClientDTO();
        clientDTO.setId(1L);
        clientDTO.setNom("Dupont");
        clientDTO.setPrenom("Jean");
        clientDTO.setDateNaissance(LocalDate.of(1990, 1, 1));
        clientDTO.setNumeroPermis("PERM123456");
        clientDTO.setAdresse("123 Rue de la Paix");
        clientDTO.setTelephone("0123456789");
        clientDTO.setEmail("jean.dupont@email.com");
    }

    @Test
    @DisplayName("GET /api/clients - Devrait retourner la liste des clients")
    void getAllClients_ShouldReturnClientsList() throws Exception {
        // Given
        List<Client> clients = Arrays.asList(client);
        List<ClientDTO> clientDTOs = Arrays.asList(clientDTO);
        when(clientService.getAllClients()).thenReturn(clients);
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        // When & Then
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nom").value("Dupont"))
                .andExpect(jsonPath("$[0].prenom").value("Jean"));

        verify(clientService).getAllClients();
    }

    @Test
    @DisplayName("GET /api/clients/{id} - Devrait retourner un client")
    void getClientById_ExistingClient_ShouldReturnClient() throws Exception {
        // Given
        when(clientService.getClientById(1L)).thenReturn(client);
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        // When & Then
        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.prenom").value("Jean"))
                .andExpect(jsonPath("$.numeroPermis").value("PERM123456"));

        verify(clientService).getClientById(1L);
    }

    @Test
    @DisplayName("GET /api/clients/{id} - Devrait retourner 404 pour un client inexistant")
    void getClientById_NonExistingClient_ShouldReturn404() throws Exception {
        // Given
        when(clientService.getClientById(99L))
                .thenThrow(new BusinessException("Client non trouvé avec l'ID : 99"));

        // When & Then
        mockMvc.perform(get("/api/clients/99"))
                .andExpect(status().isNotFound());

        verify(clientService).getClientById(99L);
    }

    @Test
    @DisplayName("POST /api/clients - Devrait créer un nouveau client")
    void createClient_ValidData_ShouldCreateClient() throws Exception {
        // Given
        when(clientMapper.toEntity(any(ClientDTO.class))).thenReturn(client);
        when(clientService.createClient(any(Client.class))).thenReturn(client);
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        // When & Then
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nom").value("Dupont"));

        verify(clientService).createClient(any(Client.class));
    }

    @Test
    @DisplayName("POST /api/clients - Devrait retourner 400 pour des données invalides")
    void createClient_InvalidData_ShouldReturn400() throws Exception {
        // Given - DTO invalide (nom vide)
        clientDTO.setNom("");

        // When & Then
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any());
    }

    @Test
    @DisplayName("PUT /api/clients/{id} - Devrait mettre à jour un client")
    void updateClient_ValidData_ShouldUpdateClient() throws Exception {
        // Given
        when(clientMapper.toEntity(any(ClientDTO.class))).thenReturn(client);
        when(clientService.updateClient(eq(1L), any(Client.class))).thenReturn(client);
        when(clientMapper.toDTO(client)).thenReturn(clientDTO);

        // When & Then
        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nom").value("Dupont"));

        verify(clientService).updateClient(eq(1L), any(Client.class));
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} - Devrait supprimer un client")
    void deleteClient_ExistingClient_ShouldDeleteClient() throws Exception {
        // Given
        doNothing().when(clientService).deleteClient(1L);

        // When & Then
        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService).deleteClient(1L);
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} - Devrait retourner 404 pour un client inexistant")
    void deleteClient_NonExistingClient_ShouldReturn404() throws Exception {
        // Given
        doThrow(new BusinessException("Client non trouvé avec l'ID : 99"))
                .when(clientService).deleteClient(99L);

        // When & Then
        mockMvc.perform(delete("/api/clients/99"))
                .andExpect(status().isNotFound());

        verify(clientService).deleteClient(99L);
    }
}