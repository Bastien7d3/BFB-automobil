# Architecture en Couches - BFB Automobile

## 📋 Vue d'ensemble

Ce projet utilise une **architecture en 3 couches** classique pour séparer les responsabilités :

```
┌─────────────────────────────────────────┐
│   COUCHE PRÉSENTATION                   │
│   (presentation.controller)             │
│   - Validation des entrées (@Valid)     │
│   - Contrôle d'accès REST                │
│   - Gestion HTTP                         │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│   COUCHE LOGIQUE MÉTIER                 │
│   (business.service)                    │
│   - Manipulation des POJO                │
│   - Orchestration repository/producer   │
│   - Règles métier                        │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│   COUCHE STOCKAGE                       │
│   (data.repository + data.producer)     │
│   - Communication MongoDB                │
│   - Communication systèmes externes      │
└─────────────────────────────────────────┘
```

---

## 🏗️ Structure des packages

```
com.BFB.automobile/
├── model/                      # POJO (utilisés par toutes les couches)
│   └── Vehicule.java           # Entité + validation
│
├── presentation/               # COUCHE PRÉSENTATION
│   └── controller/
│       └── VehiculeController.java  # REST endpoints + @Valid
│
├── business/                   # COUCHE LOGIQUE MÉTIER  
│   └── service/
│       └── VehiculeService.java     # Orchestration + règles métier
│
└── data/                       # COUCHE STOCKAGE
    ├── repository/
    │   └── VehiculeRepository.java  # Accès MongoDB
    └── producer/
        ├── VehiculeProducer.java    # Interface communication externe
        └── VehiculeProducerImpl.java # Implémentation
```

---

## 🎯 Patterns utilisés

### 1. **Architecture en couches (Layered Architecture)**
- Séparation claire des responsabilités
- Chaque couche ne dépend que de la couche inférieure
- Facilite la maintenance et les tests

### 2. **MVC (Model-View-Controller)** 
- **Model** : `Vehicule.java` (POJO)
- **View** : Pas de vue (API REST uniquement)
- **Controller** : `VehiculeController.java`

### 3. **Dependency Injection (DI)**
- Injection via constructeur (recommandé par Spring)
- Couplage faible entre les composants
- Facilite les tests unitaires

**Exemple** :
```java
@Service
public class VehiculeService {
    private final VehiculeRepository vehiculeRepository;
    
    @Autowired  // Injection automatique
    public VehiculeService(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }
}
```

### 4. **Repository Pattern (Spring Data)**
- Abstraction de la couche de persistance
- Pas besoin d'écrire le code SQL/MongoDB
- Spring génère automatiquement les implémentations

**Exemple** :
```java
@Repository
public interface VehiculeRepository extends MongoRepository<Vehicule, String> {
    List<Vehicule> findByMarque(String marque);  // Requête dérivée automatique
}
```

### 5. **Service Layer**
- Encapsule la logique métier
- Orchestre les appels entre repository et producer
- Centralise les règles de validation métier

### 6. **Producer/Gateway Pattern**
- Interface pour communiquer avec systèmes externes
- Découple l'application des API externes
- Facilite le mock pour les tests

---

## 🔍 Responsabilités par couche

### COUCHE PRÉSENTATION (`VehiculeController`)
✅ **Ce qu'elle fait** :
- Réception des requêtes HTTP (GET, POST, PUT, DELETE)
- **Validation des POJO** avec `@Valid` (contraintes `@NotNull`, `@Min`)
- Transformation HTTP → appel service
- Gestion des codes de réponse (200, 201, 404, etc.)

❌ **Ce qu'elle ne fait PAS** :
- Logique métier
- Accès direct à la base de données
- Manipulation complexe des données

### COUCHE LOGIQUE MÉTIER (`VehiculeService`)
✅ **Ce qu'elle fait** :
- **Manipulation des POJO** (création, mise à jour)
- **Orchestration** : coordonne repository + producer
- Règles métier (ex: vérifier cohérence prix/cotation)
- Gestion des exceptions métier

❌ **Ce qu'elle ne fait PAS** :
- Validation des entrées HTTP (déjà fait par la couche présentation)
- Requêtes SQL/MongoDB directes

### COUCHE STOCKAGE (`VehiculeRepository` + `VehiculeProducer`)
✅ **Ce qu'elle fait** :
- **Communication avec MongoDB** (CRUD)
- **Communication avec systèmes externes** (API, queues)
- Requêtes de données personnalisées

❌ **Ce qu'elle ne fait PAS** :
- Logique métier
- Validation
- Orchestration

---

## 📝 Exemple de flux complet

**Requête** : `POST /api/vehicules`
```json
{
  "marque": "Peugeot",
  "modele": "308",
  "annee": 2022,
  "prix": 25000
}
```

**Flux** :
1. **Présentation** (`VehiculeController`) :
   - Reçoit la requête HTTP
   - Valide le POJO avec `@Valid` (vérifie `@NotNull`, `@Min`)
   - Appelle `vehiculeService.creerVehicule(vehicule)`

2. **Logique Métier** (`VehiculeService`) :
   - Appelle `vehiculeProducer.obtenirCotation()` (système externe)
   - Logique métier : compare prix vs cotation
   - Appelle `vehiculeRepository.save()` (sauvegarde MongoDB)
   - Appelle `vehiculeProducer.publierVehicule()` (notification externe)
   - Retourne le véhicule sauvegardé

3. **Stockage** :
   - **Repository** : Insère dans MongoDB
   - **Producer** : Envoie notification (simulation)

4. **Réponse** :
   - Retour HTTP 201 Created avec le véhicule créé (+ ID généré)

---

## 🚀 Endpoints disponibles

| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/vehicules` | Liste tous les véhicules |
| GET | `/api/vehicules/{id}` | Récupère un véhicule |
| POST | `/api/vehicules` | Crée un véhicule (validation) |
| PUT | `/api/vehicules/{id}` | Met à jour un véhicule |
| DELETE | `/api/vehicules/{id}` | Supprime un véhicule |
| GET | `/api/vehicules/search?marque=...` | Recherche par marque |
| GET | `/api/vehicules/recents?annee=...` | Véhicules récents |

---

## 💡 Points à mentionner au prof

### Forces de cette architecture :
1. **Séparation claire** : chaque couche a une responsabilité unique (SRP)
2. **Testabilité** : facile de mocker les dépendances (DI)
3. **Évolutivité** : on peut changer MongoDB pour PostgreSQL sans toucher au controller
4. **Maintenabilité** : code organisé, facile à comprendre
5. **Validation centralisée** : `@Valid` dans le controller garantit des données propres

### Améliorations possibles (si le prof demande) :
- Ajouter DTO pour séparer API et modèle interne
- Gestion des exceptions personnalisée (`@ControllerAdvice`)
- Tests unitaires avec JUnit + Mockito
- Documentation API avec Swagger/OpenAPI
- Sécurité avec Spring Security

---

## 🎓 Mots-clés à mentionner

- **Separation of Concerns** (séparation des préoccupations)
- **Loose Coupling** (couplage faible)
- **Dependency Injection** (inversion de contrôle)
- **Repository Pattern** (abstraction de la persistance)
- **Service Layer** (logique métier isolée)
- **Bean Validation** (JSR-303/380)
- **REST API** (architecture RESTful)

Bon courage ! 🚀
