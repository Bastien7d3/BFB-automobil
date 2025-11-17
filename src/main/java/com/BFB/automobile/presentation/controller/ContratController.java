package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.service.ContratService;
import com.BFB.automobile.data.Contrat;
import com.BFB.automobile.data.EtatContrat;
import com.BFB.automobile.presentation.dto.ContratDTO;
import com.BFB.automobile.presentation.mapper.ContratMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des contrats de location
 * Expose les endpoints de l'API pour les opérations sur les contrats
 */
@RestController
@RequestMapping("/api/contrats")
@CrossOrigin(origins = "*")
public class ContratController {
    
    private final ContratService contratService;
    private final ContratMapper contratMapper;
    
    @Autowired
    public ContratController(ContratService contratService, 
                            ContratMapper contratMapper) {
        this.contratService = contratService;
        this.contratMapper = contratMapper;
    }
    
    /**
     * GET /api/contrats - Récupère tous les contrats
     * Paramètres optionnels : etat, clientId, vehiculeId
     */
    @GetMapping
    public ResponseEntity<List<ContratDTO>> obtenirTousLesContrats(
            @RequestParam(required = false) EtatContrat etat,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId) {
        
        List<Contrat> contrats;
        
        if (clientId != null) {
            contrats = contratService.obtenirContratsParClient(clientId);
        } else if (vehiculeId != null) {
            contrats = contratService.obtenirContratsParVehicule(vehiculeId);
        } else if (etat != null) {
            contrats = contratService.obtenirContratsParEtat(etat);
        } else {
            contrats = contratService.obtenirTousLesContrats();
        }
        
        List<ContratDTO> dtos = contrats.stream()
                .map(contratMapper::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/contrats/actifs - Récupère tous les contrats actifs
     */
    @GetMapping("/actifs")
    public ResponseEntity<List<ContratDTO>> obtenirContratsActifs() {
        List<Contrat> contrats = contratService.obtenirContratsActifs();
        List<ContratDTO> dtos = contrats.stream()
                .map(contratMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/contrats/{id} - Récupère un contrat par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContratDTO> obtenirContratParId(@PathVariable Long id) {
        Contrat contrat = contratService.obtenirContratParId(id);
        return ResponseEntity.ok(contratMapper.toDTO(contrat));
    }
    
    /**
     * GET /api/contrats/client/{clientId} - Récupère tous les contrats d'un client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ContratDTO>> obtenirContratsParClient(
            @PathVariable Long clientId) {
        List<Contrat> contrats = contratService.obtenirContratsParClient(clientId);
        List<ContratDTO> dtos = contrats.stream()
                .map(contratMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/contrats/vehicule/{vehiculeId} - Récupère tous les contrats d'un véhicule
     */
    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<List<ContratDTO>> obtenirContratsParVehicule(
            @PathVariable Long vehiculeId) {
        List<Contrat> contrats = contratService.obtenirContratsParVehicule(vehiculeId);
        List<ContratDTO> dtos = contrats.stream()
                .map(contratMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * POST /api/contrats - Crée un nouveau contrat
     */
    @PostMapping
    public ResponseEntity<ContratDTO> creerContrat(
            @Valid @RequestBody ContratDTO contratDTO) {
        Contrat contrat = contratMapper.toEntity(contratDTO);
        Contrat contratCree = contratService.creerContrat(contrat);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contratMapper.toDTO(contratCree));
    }
    
    /**
     * PUT /api/contrats/{id} - Met à jour un contrat existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContratDTO> mettreAJourContrat(
            @PathVariable Long id,
            @Valid @RequestBody ContratDTO contratDTO) {
        Contrat contrat = contratMapper.toEntity(contratDTO);
        Contrat contratMisAJour = contratService.mettreAJourContrat(id, contrat);
        return ResponseEntity.ok(contratMapper.toDTO(contratMisAJour));
    }
    
    /**
     * PATCH /api/contrats/{id}/annuler - Annule un contrat
     */
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<ContratDTO> annulerContrat(
            @PathVariable Long id,
            @RequestParam(required = false) String motif) {
        Contrat contrat = contratService.annulerContrat(id, motif);
        return ResponseEntity.ok(contratMapper.toDTO(contrat));
    }
    
    /**
     * PATCH /api/contrats/{id}/terminer - Termine un contrat (retour du véhicule)
     */
    @PatchMapping("/{id}/terminer")
    public ResponseEntity<ContratDTO> terminerContrat(@PathVariable Long id) {
        Contrat contrat = contratService.terminerContrat(id);
        return ResponseEntity.ok(contratMapper.toDTO(contrat));
    }
    
    /**
     * POST /api/contrats/traiter-etats - Déclenche manuellement le traitement des états
     * (normalement exécuté automatiquement chaque nuit)
     */
    @PostMapping("/traiter-etats")
    public ResponseEntity<String> traiterChangementsEtat() {
        contratService.traiterChangementsEtatAutomatiques();
        return ResponseEntity.ok("Traitement des changements d'état effectué avec succès");
    }
}
