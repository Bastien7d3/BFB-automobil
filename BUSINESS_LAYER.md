# COUCHE BUSINESS - Explication des choix techniques

## Vue d'ensemble

La couche Business est le **cœur métier** de l'application. Elle implémente **toutes les règles métier** spécifiées dans le cahier des charges, orchestre les opérations complexes et garantit la cohérence des données.

---

## 1. Architecture et Organisation

### Structure des packages
```
com.BFB.automobile.business/
├── service/
│   ├── ClientService.java         # Gestion des clients
│   ├── VehiculeService.java      # Gestion des véhicules
│   └── ContratService.java       # Gestion des contrats (le plus complexe)
└── exception/
    └── BusinessException.java     # Exception métier personnalisée
```

---

## 2. Patterns et Principes Architecturaux

### 2.1 Pattern Service Layer (Couche de Services)

**Qu'est-ce que c'est ?**
Les services encapsulent la logique métier et orchestrent les appels aux repositories. Un service = un agrégat métier.

**✅ POURQUOI CE PATTERN ?**

1. **Séparation des responsabilités** (SRP - Single Responsibility Principle)
   - Repository = Accès données
   - Service = Logique métier
   - Controller = Exposition API

2. **Réutilisabilité**
   - Un service peut être appelé par plusieurs contrôleurs
   - Ou par des tâches planifiées, des événements, etc.

3. **Testabilité**
   - Tests unitaires du métier sans base de données (mocks)
   - Tests d'intégration avec vraie base

**Exemple de séparation claire :**
```java
// Repository : accès données (simple)
Optional<Client> findByNumeroPermis(String numeroPermis);

// Service : validation métier (complexe)
public Client creerClient(Client client) {
    // Validation âge minimum
    if (client.getDateNaissance().isAfter(LocalDate.now().minusYears(18))) {
        throw new BusinessException("AGE_INSUFFISANT", "...");
    }
    // Validation unicité
    // Sauvegarde
}
```

### 2.2 Gestion des Transactions avec @Transactional

**Qu'est-ce que c'est ?**
`@Transactional` garantit que toutes les opérations en base de données réussissent ou échouent ensemble (atomicité).

**Exemple critique : Création d'un contrat**
```java
@Transactional
public Contrat creerContrat(Contrat contrat) {
    // 1. Valider le client
    Client client = clientRepository.findById(...);
    
    // 2. Valider le véhicule
    Vehicule vehicule = vehiculeRepository.findById(...);
    
    // 3. Changer l'état du véhicule
    vehicule.setEtat(EtatVehicule.EN_LOCATION);
    vehiculeRepository.save(vehicule);
    
    // 4. Créer le contrat
    return contratRepository.save(contrat);
    
    // Si une étape échoue, TOUT est annulé (rollback)
}
```

**✅ POURQUOI C'EST CRITIQUE ?**
Sans `@Transactional`, si l'étape 4 échoue, le véhicule resterait marqué EN_LOCATION mais sans contrat → **incohérence de données !**

**@Transactional(readOnly = true) pour les lectures :**
```java
@Transactional(readOnly = true)
public List<Client> obtenirTousLesClients() {
    return clientRepository.findAll();
}
```

**✅ AVANTAGES :**
- Optimisation des performances (pas de gestion de transaction en écriture)
- Protection contre les modifications accidentelles
- Signal clair d'intention

---

## 3. Implémentation des Règles Métier

### 3.1 Règles sur les Clients

#### Règle 1 : Unicité client (nom + prénom + date de naissance)
```java
if (clientRepository.existsByNomAndPrenomAndDateNaissance(...)) {
    throw new BusinessException(
        "CLIENT_EXISTE_DEJA",
        "Un client avec ce nom, prénom et date de naissance existe déjà");
}
```

**✅ POURQUOI vérifier en plus de la contrainte DB ?**
1. **UX** : Message d'erreur clair avant d'essayer l'insertion
2. **Performance** : `exists()` plus rapide que `save()` qui échoue
3. **Contrôle** : On choisit le code d'erreur et le message

#### Règle 2 : Numéro de permis unique
```java
if (clientRepository.existsByNumeroPermis(client.getNumeroPermis())) {
    throw new BusinessException(
        "NUMERO_PERMIS_EXISTE",
        "Ce numéro de permis est déjà utilisé par un autre client");
}
```

#### Règle 3 (ajoutée) : Âge minimum 18 ans
```java
if (client.getDateNaissance().isAfter(LocalDate.now().minusYears(18))) {
    throw new BusinessException(
        "AGE_INSUFFISANT",
        "Le client doit avoir au moins 18 ans pour louer un véhicule");
}
```

**✅ POURQUOI cette règle additionnelle ?**
- **Contexte métier réel** : Location de véhicule = assurance = 18 ans minimum
- **Amélioration proposée** (demandée dans le cahier des charges)
- **Validation métier** (pas au niveau base de données)

### 3.2 Règles sur les Véhicules

#### Règle 1 : Unicité par immatriculation
```java
if (vehiculeRepository.existsByImmatriculation(vehicule.getImmatriculation())) {
    throw new BusinessException(
        "IMMATRICULATION_EXISTE",
        "Un véhicule avec cette immatriculation existe déjà");
}
```

#### Règle 2 : Véhicules en panne → annulation contrats en attente
```java
public Vehicule changerEtatVehicule(Long id, EtatVehicule nouvelEtat) {
    Vehicule vehicule = vehiculeRepository.findById(id)...;
    
    EtatVehicule ancienEtat = vehicule.getEtat();
    vehicule.setEtat(nouvelEtat);
    
    // Règle métier automatique
    if (nouvelEtat == EtatVehicule.EN_PANNE && ancienEtat != EtatVehicule.EN_PANNE) {
        annulerContratsEnAttente(vehicule);
    }
    
    return vehiculeRepository.save(vehicule);
}

private void annulerContratsEnAttente(Vehicule vehicule) {
    List<Contrat> contratsEnAttente = contratRepository
        .findContratsEnAttenteByVehicule(vehicule.getId());
    
    for (Contrat contrat : contratsEnAttente) {
        contrat.setEtat(EtatContrat.ANNULE);
        contrat.setCommentaire(
            "Contrat annulé automatiquement : véhicule déclaré en panne");
        contratRepository.save(contrat);
    }
}
```

**✅ DESIGN PATTERN utilisé : Command Pattern**
- Changement d'état = déclencheur d'actions automatiques
- Séparation claire : cause (changement état) / effet (annulation contrats)

### 3.3 Règles sur les Contrats (LES PLUS COMPLEXES)

#### Règle 1 : Véhicules en panne ne peuvent pas être loués
```java
if (vehicule.estEnPanne()) {
    throw new BusinessException(
        "VEHICULE_EN_PANNE",
        "Ce véhicule est en panne et ne peut pas être loué");
}
```

#### Règle 2 : Un véhicule ne peut être loué qu'une fois sur une période
```java
List<Contrat> contratsConflictuels = contratRepository.findContratsConflictuels(
    vehicule.getId(),
    contrat.getDateDebut(),
    contrat.getDateFin()
);

if (!contratsConflictuels.isEmpty()) {
    throw new BusinessException(
        "VEHICULE_DEJA_LOUE",
        "Ce véhicule est déjà loué sur cette période");
}
```

**Requête de détection des conflits :**
```sql
SELECT * FROM contrats 
WHERE vehicule_id = ?
AND etat NOT IN ('ANNULE', 'TERMINE')
AND (
    (date_debut <= ? AND date_fin >= ?) -- Chevauchement
)
```

**✅ Algorithme de détection de chevauchement :**
Deux périodes [A1, A2] et [B1, B2] se chevauchent si :
```
!(A2 < B1 OR A1 > B2)
```
Équivalent à :
```
A1 <= B2 AND A2 >= B1
```

#### Règle 3 : Un client peut louer plusieurs véhicules simultanément
**Pas de restriction implémentée** → C'est le comportement par défaut !

Si on voulait l'interdire, on ajouterait :
```java
List<Contrat> contratsClientSurPeriode = contratRepository
    .findContratsClientSurPeriode(clientId, dateDebut, dateFin);

if (!contratsClientSurPeriode.isEmpty()) {
    throw new BusinessException("CLIENT_DEJA_EN_LOCATION", "...");
}
```

#### Règle 4 : Gestion automatique des retards

**Implémentation : Tâche planifiée (Scheduled Task)**
```java
@Scheduled(cron = "0 0 0 * * *") // Tous les jours à minuit
public void traiterChangementsEtatAutomatiques() {
    LocalDate aujourdhui = LocalDate.now();
    
    demarrerContratsAujourdhui(aujourdhui);
    marquerContratsEnRetard(aujourdhui);
    annulerContratsBloquesParRetard(aujourdhui);
}
```

**✅ POURQUOI une tâche planifiée ?**
1. **Automatisation** : Pas besoin d'intervention humaine
2. **Cohérence** : Traitement systématique chaque jour
3. **Performance** : Exécution hors heures de pointe (minuit)

**Sous-règles implémentées :**

**a) Démarrage automatique des contrats**
```java
private void demarrerContratsAujourdhui(LocalDate aujourdhui) {
    List<Contrat> contratsADemarrer = contratRepository
        .findContratsADemarrerAujourdhui(aujourdhui);
    
    for (Contrat contrat : contratsADemarrer) {
        if (contrat.getVehicule().estDisponible()) {
            contrat.setEtat(EtatContrat.EN_COURS);
            contrat.getVehicule().setEtat(EtatVehicule.EN_LOCATION);
            // Sauvegarde en base
        }
    }
}
```

**b) Marquage des retards**
```java
private void marquerContratsEnRetard(LocalDate aujourdhui) {
    List<Contrat> contratsEnRetard = contratRepository
        .findContratsEnRetard(aujourdhui);
    
    for (Contrat contrat : contratsEnRetard) {
        contrat.setEtat(EtatContrat.EN_RETARD);
        contrat.setCommentaire("Contrat en retard depuis le " + contrat.getDateFin());
    }
}
```

**c) Annulation des contrats bloqués par un retard**
```java
private void annulerContratsBloquesParRetard(LocalDate aujourdhui) {
    // Trouver les contrats en attente qui devaient commencer
    List<Contrat> contratsEnAttente = contratRepository
        .findByEtat(EtatContrat.EN_ATTENTE).stream()
        .filter(c -> !c.getDateDebut().isAfter(aujourdhui))
        .toList();
    
    for (Contrat contrat : contratsEnAttente) {
        // Vérifier si bloqué par un retard
        List<Contrat> contratsEnRetardPourCeVehicule = ...
        
        if (!contratsEnRetardPourCeVehicule.isEmpty()) {
            contrat.setEtat(EtatContrat.ANNULE);
            contrat.setCommentaire(
                "Contrat annulé automatiquement : véhicule bloqué par un retard");
        }
    }
}
```

**✅ DESIGN PATTERN : State Machine (Machine à états)**
Les contrats passent par des états définis :
```
EN_ATTENTE → EN_COURS → TERMINE
           ↓          ↓
         ANNULE    EN_RETARD → TERMINE
```

---

## 4. Gestion des Exceptions Métier

### Pattern Exception Personnalisée

**Pourquoi BusinessException et pas Exception standard ?**

```java
public class BusinessException extends RuntimeException {
    private final String code;
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
```

**✅ AVANTAGES :**

1. **Code d'erreur structuré** : `CLIENT_EXISTE_DEJA`, `VEHICULE_EN_PANNE`, etc.
2. **RuntimeException** : Pas besoin de `try-catch` partout
3. **Gestion centralisée** : Le `GlobalExceptionHandler` transforme en HTTP 400
4. **Traçabilité** : Logs avec code d'erreur + message

**Exemple de flux complet :**
```
Service : throw new BusinessException("VEHICULE_EN_PANNE", "...")
    ↓
GlobalExceptionHandler : Capture l'exception
    ↓
Retourne : HTTP 400 avec { "code": "VEHICULE_EN_PANNE", "message": "..." }
```

---

## 5. Injection de Dépendances et Testabilité

### Constructor Injection (recommandé par Spring)

```java
@Service
public class ContratService {
    private final ContratRepository contratRepository;
    private final ClientRepository clientRepository;
    private final VehiculeRepository vehiculeRepository;
    
    @Autowired
    public ContratService(ContratRepository contratRepository,
                         ClientRepository clientRepository,
                         VehiculeRepository vehiculeRepository) {
        this.contratRepository = contratRepository;
        this.clientRepository = clientRepository;
        this.vehiculeRepository = vehiculeRepository;
    }
}
```

**✅ POURQUOI Constructor Injection plutôt que @Autowired sur les champs ?**

| Critère | Field Injection | Constructor Injection |
|---------|----------------|----------------------|
| Immutabilité | ❌ Non (`private` non-final) | ✅ Oui (`final`) |
| Testabilité | ❌ Difficile (reflexion nécessaire) | ✅ Facile (new Service(...)) |
| Dépendances visibles | ❌ Cachées dans la classe | ✅ Visibles dans le constructeur |
| Injection circulaire | ❌ Erreur au runtime | ✅ Erreur à la compilation |

**Exemple de test unitaire facilité :**
```java
@Test
void testCreerClient() {
    // Mocks
    ClientRepository mockRepo = mock(ClientRepository.class);
    
    // Injection manuelle (pas besoin de Spring)
    ClientService service = new ClientService(mockRepo);
    
    // Test...
}
```

---

## 6. Optimisations et Bonnes Pratiques

### 6.1 Méthodes @Transactional(readOnly = true)

**Optimisation des lectures :**
```java
@Transactional(readOnly = true)
public List<Vehicule> obtenirVehiculesDisponibles() {
    return vehiculeRepository.findByEtat(EtatVehicule.DISPONIBLE);
}
```

**✅ BÉNÉFICES :**
- Pas de flush en fin de transaction (gain de performance)
- Optimisation JDBC (mode read-only)
- Protection contre modifications accidentelles

### 6.2 Éviter les N+1 queries

**Problème potentiel :**
```java
List<Contrat> contrats = contratRepository.findAll();
for (Contrat c : contrats) {
    System.out.println(c.getClient().getNom()); // 1 requête par contrat !
}
```

**Solution : Fetch Join**
```java
@Query("SELECT c FROM Contrat c JOIN FETCH c.client JOIN FETCH c.vehicule")
List<Contrat> findAllWithDetails();
```

### 6.3 Validation en cascade

**Pattern : Fail Fast**
```java
public Contrat creerContrat(Contrat contrat) {
    // Validation 1 : Client
    Client client = clientRepository.findById(...)
        .orElseThrow(() -> new BusinessException("CLIENT_NON_TROUVE", "..."));
    
    // Validation 2 : Véhicule
    Vehicule vehicule = vehiculeRepository.findById(...)
        .orElseThrow(() -> new BusinessException("VEHICULE_NON_TROUVE", "..."));
    
    // Validation 3 : Dates
    if (contrat.getDateDebut().isAfter(contrat.getDateFin())) {
        throw new BusinessException("DATES_INCOHERENTES", "...");
    }
    
    // Validation 4 : Disponibilité
    // ...
}
```

**✅ POURQUOI valider dans cet ordre ?**
1. Échouer vite (données manquantes)
2. Validations simples avant complexes
3. Éviter requêtes inutiles si données invalides

---

## 7. Évolutions Futures Facilitées

### 7.1 Ajout de notifications

```java
@Autowired
private NotificationService notificationService; // Service à créer

private void marquerContratsEnRetard(LocalDate aujourdhui) {
    List<Contrat> contratsEnRetard = ...;
    
    for (Contrat contrat : contratsEnRetard) {
        contrat.setEtat(EtatContrat.EN_RETARD);
        
        // NOUVEAU : Envoi email/SMS au client
        notificationService.envoyerAlerte(
            contrat.getClient(),
            "Votre location est en retard"
        );
    }
}
```

### 7.2 Ajout de règles tarifaires

```java
@Service
public class TarifService {
    public BigDecimal calculerPrixLocation(Contrat contrat) {
        long nombreJours = ChronoUnit.DAYS.between(
            contrat.getDateDebut(), 
            contrat.getDateFin()
        );
        
        BigDecimal tarifJournalier = contrat.getVehicule().getTarifJournalier();
        BigDecimal prixBase = tarifJournalier.multiply(BigDecimal.valueOf(nombreJours));
        
        // Réductions selon durée, client fidèle, etc.
        return prixBase;
    }
}
```

### 7.3 Ajout d'un historique des modifications

**Pattern : Event Sourcing (simplifié)**
```java
@Service
public class HistoriqueService {
    public void enregistrerModification(String entite, Long id, String action) {
        // Sauvegarder dans une table d'audit
    }
}

// Utilisation dans VehiculeService
vehicule.setEtat(nouvelEtat);
vehiculeRepository.save(vehicule);
historiqueService.enregistrerModification("VEHICULE", vehicule.getId(), "CHANGEMENT_ETAT");
```

---

## 8. Résumé des Choix Techniques

| Décision | Justification |
|----------|---------------|
| **Service Layer Pattern** | Séparation responsabilités, réutilisabilité, testabilité |
| **@Transactional** | Atomicité, cohérence des données, rollback automatique |
| **BusinessException** | Codes d'erreur structurés, gestion centralisée |
| **Constructor Injection** | Immutabilité, testabilité, visibilité dépendances |
| **@Scheduled** | Automatisation règles temporelles, cohérence |
| **readOnly = true** | Optimisation lectures, protection modifications |
| **Fail Fast validation** | Performance, expérience utilisateur |
| **State Machine** | Clarté des transitions d'états, maintenabilité |

---

## 9. Points d'Attention pour la Soutenance

**Questions probables :**

1. **"Pourquoi des services séparés pour chaque entité ?"**
   → SRP, testabilité, évolutivité (ajout Facture, Assurance, etc.)

2. **"Comment gérez-vous les transactions ?"**
   → @Transactional garantit l'atomicité. Exemple contrat → véhicule

3. **"Que se passe-t-il en cas de double réservation ?"**
   → Requête de détection de conflits + transaction isolée

4. **"Comment assurez-vous l'exécution quotidienne des tâches ?"**
   → @Scheduled avec cron expression. Production : utiliser Quartz ou jobs Kubernetes

5. **"Pourquoi BusinessException et pas @Valid ?"**
   → @Valid = validation structurelle (format, non-null)
   → BusinessException = validation métier (âge 18 ans, disponibilité véhicule)

6. **"Comment testez-vous ces services ?"**
   → Tests unitaires (mocks) + tests d'intégration (vraie base H2)

---

**Prêt à défendre une architecture métier solide et évolutive !** 🎯
