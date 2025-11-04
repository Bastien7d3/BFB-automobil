package com.BFB.automobile.presentation.controller;

import com.BFB.automobile.business.service.VehiculeService;
import com.BFB.automobile.data.Vehicule;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * COUCHE PRÉSENTATION - Controller REST
 * Gère les requêtes HTTP et délègue la logique métier au service.
 */
@RestController
@RequestMapping("/api/vehicules") // PATTERN: Centralized Request Mapping - Tous les endpoints commencent par /api/vehicules
public class VehiculeController {
    
    // ========== PATTERN: Dependency Injection (Constructor Injection) ==========
    // POURQUOI constructeur et non @Autowired sur le champ:
    // - Immutabilité (final) → thread-safe
    // - Testabilité (on peut passer un mock dans les tests)
    // - Obligation explicite (Spring échoue si la dépendance manque)
    // - Recommandation officielle Spring
    private final VehiculeService vehiculeService;
    
    @Autowired // Spring injecte automatiquement VehiculeService (IoC - Inversion of Control)
    public VehiculeController(VehiculeService vehiculeService) {
        this.vehiculeService = vehiculeService;
    }
    
    /**
     * ========== ENDPOINT: GET /api/vehicules ==========
     * PATTERN: Query Method (récupération de liste)
     * POURQUOI ResponseEntity: contrôle fin du code HTTP et des headers
     */
    @GetMapping // Équivalent à @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Vehicule>> obtenirTousLesVehicules() {
        List<Vehicule> vehicules = vehiculeService.obtenirTousLesVehicules();
        return ResponseEntity.ok(vehicules); // HTTP 200 OK + body JSON
    }
    
    /**
     * ========== ENDPOINT: GET /api/vehicules/{id} ==========
     * PATTERN: Path Variable - L'ID est dans l'URL
     * PATTERN: Optional Pattern - Gestion élégante de l'absence de résultat
     */
    @GetMapping("/{id}")
    public ResponseEntity<Vehicule> obtenirVehiculeParId(@PathVariable String id) {
        // Le service retourne Optional<Vehicule>
        // Si présent → HTTP 200 + véhicule
        // Si absent → HTTP 404
        return vehiculeService.obtenirVehiculeParId(id)
            .map(ResponseEntity::ok)              // Présent: 200 OK
            .orElse(ResponseEntity.notFound().build()); // Absent: 404 Not Found
    }
    
    /**
     * ========== ENDPOINT: POST /api/vehicules ==========
     * PATTERN: Command Method (création)
     * PATTERN: Bean Validation - @Valid déclenche la validation AVANT l'exécution
     * 
     * POURQUOI @Valid ICI:
     * - Validation des entrées utilisateur (couche présentation)
     * - Si validation échoue → HTTP 400 Bad Request automatique
     * - Garantit que le service reçoit des données valides
     * 
     * FLUX:
     * 1. Spring désérialise JSON → objet Vehicule
     * 2. @Valid déclenche validation (@NotNull, @Min, etc.)
     * 3. Si OK → appel vehiculeService.creerVehicule()
     * 4. Si KO → exception MethodArgumentNotValidException → HTTP 400
     */
    @PostMapping
    public ResponseEntity<Vehicule> creerVehicule(@Valid @RequestBody Vehicule vehicule) {
        // À ce stade, le véhicule est garanti valide (grâce à @Valid)
        Vehicule vehiculeCree = vehiculeService.creerVehicule(vehicule);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculeCree); // HTTP 201 Created
    }
    
    /**
     * ========== ENDPOINT: PUT /api/vehicules/{id} ==========
     * PATTERN: Command Method (modification)
     * PATTERN: Path Variable + Request Body
     */
    @PutMapping("/{id}")
    public ResponseEntity<Vehicule> mettreAJourVehicule(
            @PathVariable String id,
            @Valid @RequestBody Vehicule vehicule) { // @Valid pour valider les modifications
        try {
            Vehicule vehiculeMaj = vehiculeService.mettreAJourVehicule(id, vehicule);
            return ResponseEntity.ok(vehiculeMaj); // HTTP 200 OK
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // HTTP 404 si véhicule introuvable
        }
    }
    
    /**
     * ========== ENDPOINT: DELETE /api/vehicules/{id} ==========
     * PATTERN: Command Method (suppression)
     * POURQUOI ResponseEntity<Void>: pas de body dans la réponse
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerVehicule(@PathVariable String id) {
        vehiculeService.supprimerVehicule(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content (succès sans body)
    }
    
    /**
     * ========== ENDPOINT: GET /api/vehicules/search?marque=Peugeot ==========
     * PATTERN: Query Parameter - Paramètre dans l'URL après "?"
     * POURQUOI @RequestParam: extraction automatique du paramètre
     */
    @GetMapping("/search")
    public ResponseEntity<List<Vehicule>> rechercherParMarque(
            @RequestParam(required = false) String marque) { // required=false → paramètre optionnel
        if (marque != null && !marque.isEmpty()) {
            List<Vehicule> vehicules = vehiculeService.rechercherParMarque(marque);
            return ResponseEntity.ok(vehicules);
        }
        return ResponseEntity.badRequest().build(); // HTTP 400 si marque manquante
    }
    
    /**
     * ========== ENDPOINT: GET /api/vehicules/recents?annee=2020 ==========
     * PATTERN: Query Parameter avec valeur par défaut
     */
    @GetMapping("/recents")
    public ResponseEntity<List<Vehicule>> obtenirVehiculesRecents(
            @RequestParam(defaultValue = "2015") Integer annee) { // defaultValue si paramètre absent
        List<Vehicule> vehicules = vehiculeService.obtenirVehiculesRecents(annee);
        return ResponseEntity.ok(vehicules);
    }
}
