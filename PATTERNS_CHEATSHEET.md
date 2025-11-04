# 📚 Récapitulatif des Patterns - Cheat Sheet pour le prof

## 🎯 Vue d'ensemble par fichier

| Fichier | Couche | Patterns principaux |
|---------|--------|---------------------|
| `Vehicule.java` | Model | POJO, Entity, Bean Validation |
| `VehiculeController.java` | Présentation | MVC, REST, DI, Bean Validation |
| `VehiculeService.java` | Logique Métier | Service Layer, Facade, Orchestration, DI |
| `VehiculeRepository.java` | Stockage | Repository, DAO, Derived Queries |
| `VehiculeProducer.java` | Stockage | Gateway, Interface Segregation |
| `VehiculeProducerImpl.java` | Stockage | Component, Stub/Mock |

---

## 📋 Liste complète des patterns utilisés

### 1️⃣ **Architecture globale**

#### **Layered Architecture (Architecture en couches)**
- **Où**: Toute l'application
- **Quoi**: Séparation en 3 couches (Présentation, Métier, Stockage)
- **Pourquoi**: Séparation des responsabilités, maintenabilité, testabilité
- **Dire au prof**: "J'ai séparé l'application en 3 couches pour respecter le principe de séparation des responsabilités. Chaque couche a un rôle bien défini."

---

### 2️⃣ **Couche PRÉSENTATION** (`VehiculeController.java`)

#### **MVC (Model-View-Controller)**
- **Où**: `VehiculeController.java`
- **Quoi**: 
  - Model = `Vehicule.java`
  - Controller = `VehiculeController.java`
  - View = absente (API REST)
- **Pourquoi**: Séparation présentation ↔ logique métier
- **Dire au prof**: "J'utilise le pattern MVC. Le controller gère les requêtes HTTP, le modèle représente les données, et il n'y a pas de vue car c'est une API REST."

#### **REST (Representational State Transfer)**
- **Où**: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- **Quoi**: Architecture API avec verbes HTTP standardisés
- **Pourquoi**: Standard universel, interopérable, stateless
- **Dire au prof**: "J'ai créé une API REST avec les verbes HTTP standards : GET pour lire, POST pour créer, PUT pour modifier, DELETE pour supprimer."

#### **Dependency Injection (Constructor Injection)**
- **Où**: Constructeur de `VehiculeController`
- **Quoi**: Spring injecte automatiquement `VehiculeService`
- **Pourquoi**: Couplage faible, testabilité, immutabilité
- **Dire au prof**: "J'utilise l'injection par constructeur, recommandée par Spring. Ça me permet d'avoir un couplage faible et de facilement mocker le service dans les tests."

#### **Bean Validation (JSR-303/380)**
- **Où**: `@Valid` dans les méthodes du controller
- **Quoi**: Validation automatique des POJO avant exécution
- **Pourquoi**: Validation centralisée, cohérente, automatique
- **Dire au prof**: "La validation se fait avec @Valid dans le controller. Si les contraintes du POJO ne sont pas respectées, Spring retourne automatiquement une erreur 400."

#### **ResponseEntity Pattern**
- **Où**: Type de retour des méthodes
- **Quoi**: Contrôle fin des codes HTTP et headers
- **Pourquoi**: Flexibilité (200, 201, 404, 400, etc.)
- **Dire au prof**: "J'utilise ResponseEntity pour avoir un contrôle précis sur les codes HTTP retournés (200 OK, 201 Created, 404 Not Found, etc.)."

---

### 3️⃣ **Couche LOGIQUE MÉTIER** (`VehiculeService.java`)

#### **Service Layer Pattern**
- **Où**: `VehiculeService.java`
- **Quoi**: Couche qui encapsule la logique métier
- **Pourquoi**: Centralisation logique métier, réutilisabilité
- **Dire au prof**: "Le service contient toute la logique métier. Le controller se contente de recevoir les requêtes et de déléguer au service."

#### **Facade Pattern**
- **Où**: Méthodes du service
- **Quoi**: Interface simplifiée pour opérations complexes
- **Pourquoi**: Cache la complexité d'orchestration
- **Dire au prof**: "Le service fait office de facade : le controller appelle une seule méthode, et le service orchestre repository + producer en interne."

#### **Orchestration Pattern**
- **Où**: `creerVehicule()` dans le service
- **Quoi**: Coordination de plusieurs opérations (producer, repository)
- **Pourquoi**: Logique métier complexe nécessite coordination
- **Dire au prof**: "Dans la création d'un véhicule, j'orchestre trois étapes : récupération cotation, sauvegarde en base, puis publication vers système externe."

#### **Guard Clause**
- **Où**: `mettreAJourVehicule()` - vérification d'existence
- **Quoi**: Vérification préalable avant traitement
- **Pourquoi**: Fail-fast, clarté du code
- **Dire au prof**: "J'utilise une guard clause pour vérifier que le véhicule existe avant de le mettre à jour. Si absent, je lève une exception."

#### **Optional Pattern**
- **Où**: `obtenirVehiculeParId()` retourne `Optional<Vehicule>`
- **Quoi**: Gestion élégante de l'absence de résultat
- **Pourquoi**: Évite NullPointerException, force gestion du cas absent
- **Dire au prof**: "J'utilise Optional pour gérer proprement le cas où un véhicule n'existe pas, au lieu de retourner null."

---

### 4️⃣ **Couche STOCKAGE** (`VehiculeRepository.java` + `VehiculeProducer`)

#### **Repository Pattern / DAO**
- **Où**: `VehiculeRepository.java`
- **Quoi**: Abstraction de la persistance
- **Pourquoi**: On peut changer MongoDB pour PostgreSQL sans toucher au service
- **Dire au prof**: "Le repository abstrait la persistance. Si je veux changer de base de données, je n'ai qu'à modifier le repository, pas le service."

#### **Spring Data Repository**
- **Où**: `extends MongoRepository<Vehicule, String>`
- **Quoi**: Spring génère automatiquement les implémentations
- **Pourquoi**: Gain de temps, pas de code boilerplate
- **Dire au prof**: "Spring Data génère automatiquement toutes les méthodes CRUD. Je n'ai pas besoin d'écrire de code pour save(), findById(), etc."

#### **Derived Query Methods**
- **Où**: `findByMarque()`, `findByAnneeGreaterThan()`
- **Quoi**: Spring génère les requêtes à partir du nom de méthode
- **Pourquoi**: Pas besoin d'écrire les requêtes MongoDB manuellement
- **Dire au prof**: "Spring Data génère les requêtes MongoDB à partir du nom de la méthode. 'findByMarque' devient automatiquement une requête MongoDB sur le champ 'marque'."

#### **Gateway Pattern / Producer Pattern**
- **Où**: `VehiculeProducer.java`
- **Quoi**: Interface pour communication avec systèmes externes
- **Pourquoi**: Découple l'application des APIs externes
- **Dire au prof**: "Le producer est une gateway vers les systèmes externes (APIs, Kafka, etc.). Ça découple mon application des détails d'implémentation externes."

#### **Interface Segregation (SOLID)**
- **Où**: Séparation `VehiculeRepository` et `VehiculeProducer`
- **Quoi**: Interfaces dédiées à des responsabilités spécifiques
- **Pourquoi**: Chaque interface a un rôle unique
- **Dire au prof**: "J'ai séparé le repository (MongoDB) et le producer (systèmes externes) en deux interfaces distinctes pour respecter le principe de ségrégation des interfaces."

---

### 5️⃣ **Couche MODEL** (`Vehicule.java`)

#### **POJO (Plain Old Java Object)**
- **Où**: `Vehicule.java`
- **Quoi**: Objet Java simple sans dépendance framework
- **Pourquoi**: Simplicité, réutilisabilité entre couches
- **Dire au prof**: "Vehicule est un POJO, un objet Java simple. Il est utilisé par toutes les couches de l'application."

#### **Entity Pattern / Document Pattern**
- **Où**: `@Document(collection = "vehicules")`
- **Quoi**: Mapping objet ↔ collection MongoDB
- **Pourquoi**: Persistance objet-relationnel (ORM/ODM)
- **Dire au prof**: "L'annotation @Document fait le mapping entre l'objet Java et la collection MongoDB 'vehicules'."

#### **Bean Validation (JSR-303/380)**
- **Où**: `@NotNull`, `@Min` sur les champs
- **Quoi**: Contraintes de validation déclaratives
- **Pourquoi**: Validation centralisée, réutilisable
- **Dire au prof**: "Les contraintes de validation sont définies directement sur le modèle avec @NotNull et @Min. Elles sont déclenchées automatiquement par @Valid dans le controller."

#### **JavaBean Convention**
- **Où**: Constructeur par défaut + getters/setters
- **Quoi**: Convention pour frameworks Java
- **Pourquoi**: Requis par Spring, Jackson, frameworks de validation
- **Dire au prof**: "Je respecte la convention JavaBean : constructeur par défaut, getters et setters. C'est requis pour la sérialisation JSON et Spring Data."

#### **Encapsulation**
- **Où**: Champs privés + getters/setters
- **Quoi**: Protection des données internes
- **Pourquoi**: Principe OOP fondamental
- **Dire au prof**: "Les champs sont privés et accessibles via getters/setters pour respecter l'encapsulation."

---

## 🎓 Principes SOLID appliqués

### **S - Single Responsibility Principle**
- **Où**: Chaque classe a une seule responsabilité
- **Exemple**: Controller (HTTP), Service (métier), Repository (persistance)

### **O - Open/Closed Principle**
- **Où**: On peut étendre via interfaces sans modifier le code
- **Exemple**: Ajouter nouvelle implémentation de VehiculeProducer

### **L - Liskov Substitution Principle**
- **Où**: VehiculeProducerImpl peut remplacer VehiculeProducer
- **Exemple**: Mock/Stub pour les tests

### **I - Interface Segregation Principle**
- **Où**: Repository et Producer sont des interfaces séparées
- **Exemple**: Pas d'interface monolithique

### **D - Dependency Inversion Principle**
- **Où**: Service dépend d'interfaces, pas d'implémentations concrètes
- **Exemple**: VehiculeService dépend de VehiculeProducer (interface), pas de VehiculeProducerImpl

---

## 💡 Phrases clés à dire au prof

### Architecture générale
> "J'ai mis en place une **architecture en couches** avec séparation claire des responsabilités : présentation, logique métier, et stockage."

### Injection de dépendances
> "J'utilise **l'injection par constructeur** recommandée par Spring, qui garantit l'immutabilité et facilite les tests."

### Validation
> "La validation se fait de manière **déclarative** avec les annotations @NotNull et @Min sur le modèle, déclenchée par @Valid dans le controller."

### Repository
> "Le **Repository Pattern** abstrait la persistance. Spring Data génère automatiquement les implémentations, y compris les **requêtes dérivées** à partir du nom des méthodes."

### Orchestration
> "Le service **orchestre** les appels au repository et au producer, centralisant ainsi la logique métier."

### Producer
> "Le **Gateway Pattern** (Producer) découple mon application des systèmes externes. Si l'API externe change, seul le producer est impacté."

### REST
> "J'ai créé une **API REST** avec les verbes HTTP standards et les codes de réponse appropriés (200, 201, 404, 400)."

---

## 🚀 Si le prof demande "Pourquoi ces choix ?"

**Architecture en couches**
→ Séparation des responsabilités, maintenabilité, évolutivité, testabilité

**Injection de dépendances**
→ Couplage faible, testabilité (mock facile), flexibilité

**Repository Pattern**
→ Abstraction de la persistance, changement de BDD sans impact sur le métier

**Service Layer**
→ Centralisation logique métier, réutilisabilité, orchestration

**Gateway/Producer**
→ Découplage des systèmes externes, anti-corruption layer

**Bean Validation**
→ Validation centralisée, cohérente, réutilisable, déclarative

---

Bonne chance ! Tu maîtrises maintenant tous les patterns 💪🚀
