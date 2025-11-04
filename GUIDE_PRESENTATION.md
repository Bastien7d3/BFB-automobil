# 🎯 Guide rapide pour la présentation au prof

## Ce que j'ai fait cette semaine (30 min de travail)

### ✅ Implémentation d'une architecture en couches

J'ai mis en place une **architecture en 3 couches** avec séparation des responsabilités :

1. **Couche Présentation** → REST Controller + validation
2. **Couche Logique Métier** → Service + orchestration  
3. **Couche Stockage** → Repository + Producer

---

## 📁 Fichiers créés

```
src/main/java/com/BFB/automobile/
├── model/Vehicule.java                           # POJO + validation
├── presentation/controller/VehiculeController.java  # REST endpoints
├── business/service/VehiculeService.java           # Logique métier
└── data/
    ├── repository/VehiculeRepository.java          # MongoDB
    └── producer/VehiculeProducer*.java             # Système externe
```

**Total : 6 fichiers + config**

---

## 🗣️ Ce que je dis au prof

### "Qu'est-ce que tu as fait ?"

> "J'ai mis en place une architecture en couches pour le projet automobile. J'ai séparé la logique en 3 couches : présentation, métier et stockage. Ça permet de respecter le principe de séparation des responsabilités."

### "Explique-moi l'architecture"

> **COUCHE PRÉSENTATION** (`VehiculeController`)
> - Gère les requêtes HTTP REST (GET, POST, PUT, DELETE)
> - Fait la validation des entrées avec `@Valid` 
> - Utilise les annotations `@RestController` et `@RequestMapping`
> 
> **COUCHE LOGIQUE MÉTIER** (`VehiculeService`)
> - Contient la logique métier et les règles
> - Orchestre les appels vers le repository et le producer
> - Utilise `@Service` et l'injection de dépendances
> 
> **COUCHE STOCKAGE** (`VehiculeRepository` + `VehiculeProducer`)
> - Repository : communication avec MongoDB via Spring Data
> - Producer : interface pour communiquer avec des systèmes externes
> - Le repository étend `MongoRepository` pour avoir le CRUD automatique

### "Quels patterns tu as utilisés ?"

1. **MVC (Model-View-Controller)**
   - Model = `Vehicule.java`
   - Controller = `VehiculeController.java`
   - Pas de vue (API REST)

2. **Dependency Injection**
   - J'utilise l'injection par constructeur avec `@Autowired`
   - Ça crée un couplage faible entre les composants

3. **Repository Pattern**
   - Spring Data génère automatiquement les méthodes CRUD
   - On peut aussi faire des requêtes dérivées comme `findByMarque()`

4. **Service Layer**
   - La logique métier est centralisée dans le service
   - Le controller reste simple et délègue tout au service

5. **Producer/Gateway Pattern**
   - Interface pour communiquer avec l'extérieur
   - Ça découple notre application des APIs externes

### "Pourquoi cette architecture ?"

> "Cette architecture en couches offre plusieurs avantages :
> - **Séparation des responsabilités** : chaque couche a un rôle précis
> - **Testabilité** : on peut tester chaque couche indépendamment
> - **Maintenabilité** : le code est organisé et facile à comprendre
> - **Évolutivité** : si je veux changer MongoDB pour PostgreSQL, je ne touche que la couche stockage"

### "Montre-moi la validation"

> "Dans le POJO `Vehicule.java`, j'ai mis des annotations de validation :
> - `@NotNull` pour les champs obligatoires
> - `@Min` pour vérifier que l'année > 1900 et le prix > 0
> 
> Dans le controller, j'utilise `@Valid` devant le `@RequestBody`.
> Si la validation échoue, Spring retourne automatiquement une erreur 400."

### "Comment ça communique entre les couches ?"

> "Le flow est unidirectionnel :
> 1. Le **Controller** reçoit la requête HTTP
> 2. Il appelle le **Service** (injection de dépendances)
> 3. Le **Service** orchestre : il appelle le Repository ET le Producer
> 4. Le **Repository** sauvegarde dans MongoDB
> 5. Le **Producer** envoie une notification (simulation)
> 6. Le Service retourne le résultat au Controller
> 7. Le Controller renvoie la réponse HTTP"

---

## 🚀 Si le prof veut voir ça tourner

**Option 1 : Juste montrer le code**
> "Pour l'instant c'est un squelette fonctionnel. MongoDB n'est pas encore configuré localement mais l'architecture est complète."

**Option 2 : Si tu veux vraiment lancer**
```powershell
# Installer MongoDB (Docker recommandé)
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Lancer l'application
.\mvnw.cmd spring-boot:run
```

Ensuite test avec curl/Postman :
```bash
POST http://localhost:8080/api/vehicules
{
  "marque": "Peugeot",
  "modele": "308",
  "annee": 2022,
  "prix": 25000
}
```

---

## 🎓 Vocabulaire technique à placer

- **Separation of Concerns** (séparation des préoccupations)
- **Loose Coupling** (couplage faible)
- **Dependency Injection** / **Inversion of Control**
- **SOLID principles** (surtout SRP - Single Responsibility)
- **Bean Validation** (JSR-303/380)
- **REST API** / **RESTful**
- **DAO Pattern** (via Repository)

---

## 📝 Si le prof demande "Et après ?"

Améliorations possibles :
- Ajouter des **DTO** pour séparer l'API du modèle interne
- Mettre en place des **tests unitaires** (JUnit + Mockito)
- Ajouter une gestion d'**exceptions personnalisée** (`@ControllerAdvice`)
- Documentation API avec **Swagger/OpenAPI**
- **Pagination** pour les listes de véhicules
- **Sécurité** avec Spring Security (authentification JWT)

---

## ⚡ En cas de question piège

**"Pourquoi pas de DTO ?"**
> "Pour un MVP, le POJO suffit. Mais dans un vrai projet, j'utiliserais des DTO pour séparer la couche API de la couche métier et éviter d'exposer directement les entités."

**"Comment tu gères les transactions ?"**
> "Spring gère automatiquement les transactions avec `@Transactional` sur les méthodes de service si besoin. Pour MongoDB c'est moins critique car pas de relations complexes."

**"Et la sécurité ?"**
> "Actuellement c'est un squelette. Dans un vrai projet, j'ajouterais Spring Security avec authentification JWT et des rôles (USER, ADMIN)."

---

## 💪 Conseil final

**Sois confiant mais humble :**
> "J'ai implémenté une architecture en couches propre avec les patterns classiques. C'est un squelette fonctionnel qui respecte les bonnes pratiques Spring Boot. Il y a encore des améliorations possibles mais la base est solide."

**Bonne chance ! 🚀**
