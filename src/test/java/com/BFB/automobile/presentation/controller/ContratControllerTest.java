package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.exception.BusinessException;
import com.BFB.automobile.business.service.ContratService;
import com.BFB.automobile.data.*;
import com.BFB.automobile.presentation.dto.ContratDTO;
import com.BFB.automobile.presentation.mapper.ContratMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour ContratController
 * 
 * JUSTIFICATION COUCHE PRESENTATION :
 * - Teste les endpoints de gestion des contrats
 * - Valide les workflows de location (création → confirmation → annulation)
 * - Vérifie les paramètres de requête pour la création de contrats
 */
@WebMvcTest(ContratController.class)
@DisplayName("ContratController - Tests d'intégration")
class ContratControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContratService contratService;

    @MockBean
    private ContratMapper contratMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Contrat contrat;
    private ContratDTO contratDTO;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setId(1L);
        client.setNom("Dupont");

        Vehicule vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setImmatriculation("AB-123-CD");
        vehicule.setMarque("Peugeot");

        contrat = new Contrat();
        contrat.setId(1L);
        contrat.setClient(client);
        contrat.setVehicule(vehicule);
        contrat.setDateDebut(LocalDate.now().plusDays(1));
        contrat.setDateFin(LocalDate.now().plusDays(3));
        contrat.setPrixTotal(new BigDecimal("150.00"));
        contrat.setEtat(EtatContrat.EN_ATTENTE);

        contratDTO = new ContratDTO();
        contratDTO.setId(1L);
        contratDTO.setClientId(1L);
        contratDTO.setVehiculeId(1L);
        contratDTO.setDateDebut(LocalDate.now().plusDays(1));
        contratDTO.setDateFin(LocalDate.now().plusDays(3));
        contratDTO.setPrixTotal(new BigDecimal("150.00"));
        contratDTO.setEtat(EtatContrat.EN_ATTENTE);
    }

    @Test
    @DisplayName("GET /api/contrats - Devrait retourner la liste des contrats")
    void getAllContrats_ShouldReturnContratsList() throws Exception {
        // Given
        List<Contrat> contrats = Arrays.asList(contrat);
        when(contratService.getAllContrats()).thenReturn(contrats);
        when(contratMapper.toDTO(contrat)).thenReturn(contratDTO);

        // When & Then
        mockMvc.perform(get("/api/contrats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].clientId").value(1))
                .andExpect(jsonPath("$[0].vehiculeId").value(1));

        verify(contratService).getAllContrats();
    }

    @Test
    @DisplayName("POST /api/contrats - Devrait créer un nouveau contrat")
    void createContrat_ValidData_ShouldCreateContrat() throws Exception {
        // Given
        when(contratService.createContrat(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(contrat);
        when(contratMapper.toDTO(contrat)).thenReturn(contratDTO);

        // When & Then
        mockMvc.perform(post("/api/contrats")
                        .param("clientId", "1")
                        .param("vehiculeId", "1")
                        .param("dateDebut", LocalDate.now().plusDays(1).toString())
                        .param("dateFin", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.etat").value("EN_ATTENTE"));

        verify(contratService).createContrat(eq(1L), eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("PUT /api/contrats/{id}/confirmer - Devrait confirmer un contrat")
    void confirmerContrat_ExistingContrat_ShouldConfirmContrat() throws Exception {
        // Given
        doNothing().when(contratService).confirmerContrat(1L);

        // When & Then
        mockMvc.perform(patch("/api/contrats/1/confirmer"))
                .andExpect(status().isOk());

        verify(contratService).confirmerContrat(1L);
    }

    @Test
    @DisplayName("PUT /api/contrats/{id}/annuler - Devrait annuler un contrat")
    void annulerContrat_ExistingContrat_ShouldCancelContrat() throws Exception {
        // Given
        doNothing().when(contratService).annulerContrat(1L);

        // When & Then
        mockMvc.perform(patch("/api/contrats/1/annuler"))
                .andExpect(status().isOk());

        verify(contratService).annulerContrat(1L);
    }

    @Test
    @DisplayName("GET /api/contrats/client/{clientId} - Devrait retourner les contrats d'un client")
    void getContratsByClientId_ShouldReturnClientContracts() throws Exception {
        // Given
        List<Contrat> contratsClient = Arrays.asList(contrat);
        when(contratService.getContratsByClientId(1L)).thenReturn(contratsClient);
        when(contratMapper.toDTO(contrat)).thenReturn(contratDTO);

        // When & Then
        mockMvc.perform(get("/api/contrats/client/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].clientId").value(1));

        verify(contratService).getContratsByClientId(1L);
    }

    @Test
    @DisplayName("GET /api/contrats/{id} - Devrait retourner 404 pour un contrat inexistant")
    void getContratById_NonExistingContrat_ShouldReturn404() throws Exception {
        // Given
        when(contratService.getContratById(99L))
                .thenThrow(new BusinessException("Contrat non trouvé avec l'ID : 99"));

        // When & Then
        mockMvc.perform(get("/api/contrats/99"))
                .andExpect(status().isNotFound());

        verify(contratService).getContratById(99L);
    }

    @Test
    @DisplayName("POST /api/contrats - Devrait retourner 400 pour des dates invalides")
    void createContrat_InvalidDates_ShouldReturn400() throws Exception {
        // Given - Date de fin avant date de début
        when(contratService.createContrat(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new BusinessException("La date de fin doit être postérieure à la date de début"));

        // When & Then
        mockMvc.perform(post("/api/contrats")
                        .param("clientId", "1")
                        .param("vehiculeId", "1")
                        .param("dateDebut", LocalDate.now().plusDays(3).toString())
                        .param("dateFin", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isBadRequest());
    }
}