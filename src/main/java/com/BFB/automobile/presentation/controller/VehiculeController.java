package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.service.VehiculeService;
import com.BFB.automobile.data.EtatVehicule;
import com.BFB.automobile.data.Vehicule;
import com.BFB.automobile.presentation.dto.VehiculeDTO;
import com.BFB.automobile.presentation.mapper.VehiculeMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des véhicules
 * Expose les endpoints de l'API pour les opérations CRUD sur les véhicules
 */
@RestController
@RequestMapping("/api/vehicules")
@CrossOrigin(origins = "*")
public class VehiculeController {
    
    private final VehiculeService vehiculeService;
    private final VehiculeMapper vehiculeMapper;
    
    @Autowired
    public VehiculeController(VehiculeService vehiculeService, 
                             VehiculeMapper vehiculeMapper) {
        this.vehiculeService = vehiculeService;
        this.vehiculeMapper = vehiculeMapper;
    }
    
    /**
     * GET /api/vehicules - Récupère tous les véhicules
     * Paramètres optionnels : marque, modele, etat
     */
    @GetMapping
    public ResponseEntity<List<VehiculeDTO>> obtenirTousLesVehicules(
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) String modele,
            @RequestParam(required = false) EtatVehicule etat) {
        
        List<Vehicule> vehicules;
        
        if (etat != null) {
            vehicules = vehiculeService.obtenirVehiculesParEtat(etat);
        } else if (marque != null || modele != null) {
            vehicules = vehiculeService.rechercherVehicules(marque, modele);
        } else {
            vehicules = vehiculeService.obtenirTousLesVehicules();
        }
        
        List<VehiculeDTO> dtos = vehicules.stream()
                .map(vehiculeMapper::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/vehicules/disponibles - Récupère tous les véhicules disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<VehiculeDTO>> obtenirVehiculesDisponibles() {
        List<Vehicule> vehicules = vehiculeService.obtenirVehiculesDisponibles();
        List<VehiculeDTO> dtos = vehicules.stream()
                .map(vehiculeMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * GET /api/vehicules/{id} - Récupère un véhicule par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehiculeDTO> obtenirVehiculeParId(@PathVariable Long id) {
        Vehicule vehicule = vehiculeService.obtenirVehiculeParId(id);
        return ResponseEntity.ok(vehiculeMapper.toDTO(vehicule));
    }
    
    /**
     * GET /api/vehicules/immatriculation/{immatriculation} - Recherche par immatriculation
     */
    @GetMapping("/immatriculation/{immatriculation}")
    public ResponseEntity<VehiculeDTO> rechercherParImmatriculation(
            @PathVariable String immatriculation) {
        return vehiculeService.rechercherParImmatriculation(immatriculation)
                .map(vehicule -> ResponseEntity.ok(vehiculeMapper.toDTO(vehicule)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * POST /api/vehicules - Crée un nouveau véhicule
     */
    @PostMapping
    public ResponseEntity<VehiculeDTO> creerVehicule(
            @Valid @RequestBody VehiculeDTO vehiculeDTO) {
        Vehicule vehicule = vehiculeMapper.toEntity(vehiculeDTO);
        Vehicule vehiculeCree = vehiculeService.creerVehicule(vehicule);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehiculeMapper.toDTO(vehiculeCree));
    }
    
    /**
     * PUT /api/vehicules/{id} - Met à jour un véhicule existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehiculeDTO> mettreAJourVehicule(
            @PathVariable Long id,
            @Valid @RequestBody VehiculeDTO vehiculeDTO) {
        Vehicule vehicule = vehiculeMapper.toEntity(vehiculeDTO);
        Vehicule vehiculeMisAJour = vehiculeService.mettreAJourVehicule(id, vehicule);
        return ResponseEntity.ok(vehiculeMapper.toDTO(vehiculeMisAJour));
    }
    
    /**
     * PATCH /api/vehicules/{id}/etat - Change l'état d'un véhicule
     */
    @PatchMapping("/{id}/etat")
    public ResponseEntity<VehiculeDTO> changerEtatVehicule(
            @PathVariable Long id,
            @RequestParam EtatVehicule etat) {
        Vehicule vehicule = vehiculeService.changerEtatVehicule(id, etat);
        return ResponseEntity.ok(vehiculeMapper.toDTO(vehicule));
    }
    
    /**
     * DELETE /api/vehicules/{id} - Supprime un véhicule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerVehicule(@PathVariable Long id) {
        vehiculeService.supprimerVehicule(id);
        return ResponseEntity.noContent().build();
    }
}
