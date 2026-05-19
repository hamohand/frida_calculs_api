# 🌟 API Frida Calculs - Héritage Islamique

> API REST professionnelle pour le calcul automatique des parts d'héritage selon la loi islamique (Fiqh)

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📖 Table des Matières

- [Vue d'ensemble](#-vue-densemble)
- [Caractéristiques](#-caractéristiques)
- [Démarrage Rapide](#-démarrage-rapide)
- [Documentation](#-documentation)
- [Architecture](#-architecture)
- [Utilisation](#-utilisation)
- [Tests](#-tests)
- [Déploiement](#-déploiement)
- [Contribution](#-contribution)
- [Licence](#-licence)

---

## 🎯 Vue d'ensemble

L'API Frida Calculs est une solution complète et robuste pour calculer les parts d'héritage selon les règles de succession islamique basées sur les versets du Coran (Sourate An-Nisa 4:11-12, 176).

### Pourquoi cette API ?

- ✅ **Conforme à la Sharia** - Implémente fidèlement les règles de l'héritage islamique
- ✅ **Facile à utiliser** - API REST simple avec documentation Swagger
- ✅ **Robuste** - Validation complète, gestion d'erreurs, tests unitaires
- ✅ **Production-ready** - Monitoring, logging, health checks
- ✅ **Bien documentée** - Documentation OpenAPI 3.0, guides, exemples

---

## ✨ Caractéristiques

### Fonctionnalités Métier

- 📊 Calcul automatique des parts pour tous les types d'héritiers
- 👨‍👩‍👧‍👦 Support de compositions familiales complexes
- 🔢 Fractions simplifiées et réduites au même dénominateur
- ✅ Validation des données selon les règles islamiques
- 📝 Résultats détaillés avec résumé de la composition familiale

### Fonctionnalités Techniques

- 🔒 **Validation complète** - Bean Validation (JSR-380) + validation métier
- 🛡️ **Gestion d'erreurs** - Format standardisé RFC 7807 (Problem Details)
- 📚 **Documentation** - OpenAPI 3.0 (Swagger UI) + guides Markdown
- 🧪 **Tests** - Tests unitaires + tests d'intégration
- 📊 **Monitoring** - Spring Boot Actuator (health, metrics, info)
- 🌐 **CORS** - Configuration flexible pour différents clients
- 🔍 **Logging** - Logs structurés avec niveaux configurables

---

## 🚀 Démarrage Rapide

### Prérequis

- **Java 21** ou supérieur
- **Maven 3.9** ou supérieur
- (Optionnel) **Docker** pour le déploiement conteneurisé

### Installation et Lancement

```bash
# 1. Cloner le projet (ou naviguer vers le dossier)
cd frida_calculs_api

# 2. Compiler le projet
mvn clean package -DskipTests

# 3. Lancer l'application
mvn spring-boot:run
```

**L'API est maintenant accessible sur:** http://localhost:8080

### Vérification Rapide

```bash
# Test de santé
curl http://localhost:8080/api/v1/heritage/status

# Résultat attendu:
# API Frida Calculs - v1.0.0 - Opérationnelle ✓
```

### Premier Calcul

```bash
curl -X POST http://localhost:8080/api/v1/heritage/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "sexeDefunt": "M",
    "conjointVivant": true,
    "nbFilles": 1,
    "nbGarcons": 1
  }'
```

---

## 📚 Documentation

| Type | URL / Fichier | Description |
|------|---------------|-------------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentation interactive |
| **OpenAPI JSON** | http://localhost:8080/api-docs | Spec OpenAPI 3.0 |
| **Démarrage Rapide** | [docs/quickstart.md](docs/quickstart.md) | Démarrage en 3 minutes |
| **Guide Utilisateur** | [docs/guide.md](docs/guide.md) | Guide complet d'utilisation |
| **API Reference** | [docs/api.md](docs/api.md) | Documentation API détaillée |
| **Analyse Métier** | [docs/analyse_heritage.md](docs/analyse_heritage.md) | Rapport d'analyse du système |
| **Améliorations** | [docs/ameliorations.md](docs/ameliorations.md) | Historique des améliorations |

---

## 🏗️ Architecture

### Structure du Projet

```
src/main/java/com/med/frida_calculs_app/
├── config/              # Configurations (CORS, OpenAPI)
├── enums/               # Enums (Sexe, TypeHeritier)
├── exception/           # Exceptions personnalisées + GlobalHandler
├── model/               # DTOs et modèles de données
├── validator/           # Validateurs métier
├── CalculPartsService.java
├── CalculsPartsController.java
└── FridaCalculsAppApplication.java
```

### Stack Technique

| Composant | Technologie | Version |
|-----------|-------------|---------|
| **Backend** | Spring Boot | 3.5.0 |
| **Langage** | Java | 21 |
| **Build** | Maven | 3.9+ |
| **Documentation** | SpringDoc OpenAPI | 2.8.13 |
| **Validation** | Jakarta Validation | Inclus |
| **Monitoring** | Spring Actuator | Inclus |
| **Logging** | SLF4J + Logback | Inclus |

### Endpoints Principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/v1/heritage/calculate` | Calculer les parts d'héritage |
| `GET` | `/api/v1/heritage/status` | Vérifier le statut de l'API |
| `GET` | `/actuator/health` | Health check |
| `GET` | `/actuator/info` | Informations application |
| `GET` | `/swagger-ui.html` | Documentation interactive |

---

## 💻 Utilisation

### Exemple de Requête

```javascript
const response = await fetch('http://localhost:8080/api/v1/heritage/calculate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    sexeDefunt: 'M',
    conjointVivant: true,
    pereVivant: false,
    mereVivante: false,
    nbFilles: 2,
    nbGarcons: 1
  })
});

const result = await response.json();
console.log(result);
```

### Exemple de Réponse

```json
{
  "calculId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-12-21T10:30:00",
  "heritiers": [
    {
      "heritier": "conjoint",
      "part": { "numerateur": 3, "denominateur": 24 }
    },
    {
      "heritier": "fille",
      "part": { "numerateur": 4, "denominateur": 24 }
    },
    {
      "heritier": "garçon",
      "part": { "numerateur": 14, "denominateur": 24 }
    }
  ],
  "nombreHeritiers": 3,
  "denominateurCommun": 24,
  "calculComplet": true,
  "message": "Calcul des parts d'héritage effectué avec succès"
}
```

### Gestion des Erreurs

Toutes les erreurs suivent le format RFC 7807:

```json
{
  "timestamp": "2025-12-21T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Erreur de validation des données",
  "path": "/api/v1/heritage/calculate",
  "validationErrors": [
    {
      "field": "nbFilles",
      "rejectedValue": -1,
      "message": "Le nombre de filles ne peut pas être négatif"
    }
  ]
}
```

---

## 🧪 Tests

### Lancer les Tests

```bash
# Tous les tests
mvn test

# Tests unitaires uniquement
mvn test -Dtest=CalculPartsServiceTest

# Tests d'intégration
mvn test -Dtest=CalculsPartsControllerIntegrationTest

# Avec rapport de couverture
mvn clean verify
```

### Couverture des Tests

- ✅ **6 tests unitaires** - Service métier
- ✅ **8 tests d'intégration** - Controller REST
- ✅ Tests de validation
- ✅ Tests des cas d'erreur
- ✅ Tests des règles métier

---

## 🐳 Déploiement

### Docker

```bash
# Build l'image
docker build -t frida-calculs-api:1.0.0 .

# Lancer le container
docker run -d \
  -p 8080:8080 \
  --name frida-api \
  frida-calculs-api:1.0.0

# Vérifier les logs
docker logs -f frida-api

# Tester
curl http://localhost:8080/api/v1/heritage/status
```

### Docker Compose (à venir)

```yaml
version: '3.8'
services:
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
```

### Production

Pour le déploiement en production:
1. Modifier `application.properties` pour l'environnement prod
2. Configurer les CORS avec les domaines autorisés
3. Activer HTTPS
4. Configurer le monitoring externe
5. Mettre en place des logs centralisés

---

## 🤝 Contribution

Les contributions sont bienvenues ! Pour contribuer:

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

### Guidelines

- Suivre les conventions de code Java
- Ajouter des tests pour les nouvelles fonctionnalités
- Mettre à jour la documentation
- Respecter les principes SOLID

---

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## 👥 Auteurs

- **Équipe Frida** - *Développement initial*

---

## 📞 Support

- 📧 Email: contact@frida-heritage.com
- 🌐 Site: https://frida-heritage.com
- 📚 Documentation: Voir les fichiers Markdown dans le projet
- 🐛 Issues: Ouvrir une issue sur le repository

---

## 🙏 Remerciements

- Communauté Spring Boot
- Contributeurs OpenAPI
- Tous ceux qui ont testé et donné leur feedback

---

## 📈 Roadmap

### Version 1.x (Actuel)
- ✅ Calcul des parts selon la loi islamique
- ✅ API REST complète
- ✅ Documentation Swagger
- ✅ Tests unitaires et d'intégration
- ✅ Monitoring avec Actuator

### Version 2.0 (À venir)
- [ ] Authentification JWT
- [ ] Base de données (historique des calculs)
- [ ] Export PDF des résultats
- [ ] Calcul avec montant réel (€, $)
- [ ] Multi-language support (i18n)
- [ ] API de statistiques
- [ ] Interface web (Angular/React)

---

<div align="center">

**Fait avec ❤️ pour la communauté musulmane**

[🌟 Star ce projet](https://github.com/votre-org/frida-calculs-api) | [📝 Documentation](API_DOCUMENTATION.md) | [🐛 Reporter un bug](https://github.com/votre-org/frida-calculs-api/issues)

</div>
#   f r i d a _ c a l c u l s _ a p i  
 