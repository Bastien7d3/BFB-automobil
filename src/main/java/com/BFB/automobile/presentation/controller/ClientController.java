package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.service.ClientService;
import com.BFB.automobile.data.Client;
import com.BFB.automobile.presentation.dto.ClientDTO;
import com.BFB.automobile.presentation.mapper.ClientMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des clients
 * Expose les endpoints de l'API pour les opérations CRUD sur les clients
 * 
 * DESIGN PATTERNS GoF UTILISÉS :
 * 
 * 1. FACADE PATTERN : Ce contrôleur agit comme une façade qui simplifie l'accès
 *    aux opérations métier complexes. Il masque la complexité des validations,
 *    transformations et règles métier derrière une interface REST simple.
 * 
 * 2. ADAPTER PATTERN : Utilise ClientMapper pour convertir les entités (Client)
 *    vers des DTOs (ClientDTO) et vice-versa, adaptant ainsi la représentation
 *    interne à la représentation API.
 * 
 * 3. SINGLETON PATTERN : Spring crée une instance unique de ce contrôleur
 *    (scope singleton par défaut) qui gère toutes les requêtes HTTP.
 * 
 * 4. STRATEGY PATTERN : Spring MVC utilise différentes stratégies pour router
 *    les requêtes HTTP vers les bonnes méthodes selon le verbe HTTP (GET/POST/PUT/DELETE).
 */
@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*") // À configurer selon les besoins de sécurité
public class ClientController {
    
    private final ClientService clientService;
    private final ClientMapper clientMapper;
    
    /**
     * Injection par constructeur (immutabilité + testabilité)
     * Spring injecte automatiquement les dépendances (beans Singleton)
     */
    @Autowired
    public ClientController(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
    }
    
    /**
     * GET /api/clients - Récupère tous les clients
     * Paramètres optionnels : nom, prenom, actif
     */
    @GetMapping
    public ResponseEntity<List<ClientDTO>> obtenirTousLesClients(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) Boolean actif) {
        
        List<Client> clients;
        
        if (actif != null && actif) {
            clients = clientService.obtenirTousLesClientsActifs();
        } else if (nom != null || prenom != null) {
            clients = clientService.rechercherClients(nom, prenom);
        } else {
            clients = clientService.obtenirTousLesClients();
        }
        
        List<ClientDTO> dtos = clients.stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/clients/{id} - Récupère un client par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> obtenirClientParId(@PathVariable Long id) {
        Client client = clientService.obtenirClientParId(id);
        return ResponseEntity.ok(clientMapper.toDTO(client));
    }
    
    /**
     * GET /api/clients/permis/{numeroPermis} - Recherche par numéro de permis
     */
    @GetMapping("/permis/{numeroPermis}")
    public ResponseEntity<ClientDTO> rechercherParNumeroPermis(
            @PathVariable String numeroPermis) {
        return clientService.rechercherParNumeroPermis(numeroPermis)
                .map(client -> ResponseEntity.ok(clientMapper.toDTO(client)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * POST /api/clients - Crée un nouveau client
     */
    @PostMapping
    public ResponseEntity<ClientDTO> creerClient(@Valid @RequestBody ClientDTO clientDTO) {
        Client client = clientMapper.toEntity(clientDTO);
        Client clientCree = clientService.creerClient(client);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientMapper.toDTO(clientCree));
    }
    
    /**
     * PUT /api/clients/{id} - Met à jour un client existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> mettreAJourClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientDTO clientDTO) {
        Client client = clientMapper.toEntity(clientDTO);
        Client clientMisAJour = clientService.mettreAJourClient(id, client);
        return ResponseEntity.ok(clientMapper.toDTO(clientMisAJour));
    }
    
    /**
     * DELETE /api/clients/{id} - Désactive un client (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactiverClient(@PathVariable Long id) {
        clientService.desactiverClient(id);
        return ResponseEntity.noContent().build();
    }
}
