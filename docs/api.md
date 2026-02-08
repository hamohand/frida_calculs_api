# 📚 Documentation API Frida Calculs - Héritage Islamique

## Vue d'ensemble

API REST pour le calcul automatique des parts d'héritage selon la loi islamique (Fiqh). L'API implémente les règles de succession basées sur les versets du Coran (Sourate An-Nisa 4:11-12, 176).

**Version**: 1.0.0
**Base URL**: `http://localhost:8080/api/v1`

## 🚀 Démarrage rapide

### Prérequis
- Java 21+
- Maven 3.9+

### Installation

```bash
# Cloner le projet
git clone <repository-url>
cd frida-calculs-api

# Compiler et lancer
mvn clean install
mvn spring-boot:run
```

### Docker

```bash
# Build
docker build -t frida-calculs-api .

# Run
docker run -p 8080:8080 frida-calculs-api
```

L'API sera disponible sur `http://localhost:8080`

## 📖 Documentation interactive

Accédez à la documentation Swagger UI:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🔌 Endpoints

### 1. Calculer les parts d'héritage

**Endpoint principal (recommandé)**

```http
POST /api/v1/heritage/calculate
Content-Type: application/json
```

#### Requête

```json
{
  "sexeDefunt": "M",
  "conjointVivant": true,
  "pereVivant": false,
  "mereVivante": false,
  "nbFilles": 1,
  "nbGarcons": 1,
  "nbSoeurs": 0,
  "nbFreres": 0
}
```

**Paramètres**:

| Champ | Type | Requis | Description | Valeurs |
|-------|------|--------|-------------|---------|
| `sexeDefunt` | string | ✅ | Sexe du défunt | "M", "F" |
| `conjointVivant` | boolean | ❌ | Conjoint vivant | true/false |
| `pereVivant` | boolean | ❌ | Père vivant | true/false |
| `mereVivante` | boolean | ❌ | Mère vivante | true/false |
| `nbFilles` | integer | ❌ | Nombre de filles | 0-50 |
| `nbGarcons` | integer | ❌ | Nombre de garçons | 0-50 |
| `nbSoeurs` | integer | ❌ | Nombre de soeurs | 0-50 |
| `nbFreres` | integer | ❌ | Nombre de frères | 0-50 |

**Validations**:
- Au moins un héritier doit être présent
- Les nombres ne peuvent pas être négatifs
- Le sexe du défunt est obligatoire

#### Réponse (200 OK)

```json
{
  "calculId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-12-21T10:30:00",
  "heritiers": [
    {
      "heritier": "conjoint",
      "part": {
        "numerateur": 3,
        "denominateur": 24
      }
    },
    {
      "heritier": "fille",
      "part": {
        "numerateur": 7,
        "denominateur": 24
      }
    },
    {
      "heritier": "garçon",
      "part": {
        "numerateur": 14,
        "denominateur": 24
      }
    },
    {
      "heritier": "part restant",
      "part": {
        "numerateur": 0,
        "denominateur": 24
      }
    }
  ],
  "nombreHeritiers": 3,
  "denominateurCommun": 24,
  "partRestante": {
    "numerateur": 0,
    "denominateur": 24
  },
  "message": "Calcul des parts d'héritage effectué avec succès",
  "calculComplet": true,
  "composition": {
    "sexeDefunt": "M",
    "conjointVivant": true,
    "pereVivant": false,
    "mereVivante": false,
    "nbFilles": 1,
    "nbGarcons": 1,
    "nbSoeurs": 0,
    "nbFreres": 0
  }
}
```

**Champs de la réponse**:

| Champ | Type | Description |
|-------|------|-------------|
| `calculId` | string | Identifiant unique du calcul (UUID) |
| `timestamp` | datetime | Date et heure du calcul |
| `heritiers` | array | Liste des héritiers avec leurs parts |
| `nombreHeritiers` | integer | Nombre d'héritiers (hors part restante) |
| `denominateurCommun` | integer | Dénominateur commun pour toutes les parts |
| `partRestante` | object | Part non distribuée (si applicable) |
| `calculComplet` | boolean | Indique si tout l'héritage est distribué |
| `composition` | object | Résumé de la composition familiale |

### 2. Vérifier le statut de l'API

```http
GET /api/v1/heritage/status
```

#### Réponse (200 OK)

```
API Frida Calculs - v1.0.0 - Opérationnelle ✓
```

## ❌ Gestion des erreurs

Toutes les erreurs suivent le format standardisé RFC 7807 (Problem Details):

### Erreur de validation (400 Bad Request)

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

### Composition familiale invalide (400 Bad Request)

```json
{
  "timestamp": "2025-12-21T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Aucun héritier n'a été spécifié. Au moins un héritier doit être présent.",
  "path": "/api/v1/heritage/calculate"
}
```

### Erreur serveur (500 Internal Server Error)

```json
{
  "timestamp": "2025-12-21T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Une erreur inattendue s'est produite",
  "path": "/api/v1/heritage/calculate"
}
```

## 📊 Codes de statut HTTP

| Code | Description |
|------|-------------|
| 200 | Succès - Calcul effectué correctement |
| 400 | Bad Request - Données invalides |
| 500 | Erreur interne du serveur |

## 🧪 Exemples d'utilisation

### Exemple 1: Défunt masculin avec conjoint et enfants

```bash
curl -X POST http://localhost:8080/api/v1/heritage/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "sexeDefunt": "M",
    "conjointVivant": true,
    "pereVivant": false,
    "mereVivante": false,
    "nbFilles": 2,
    "nbGarcons": 1,
    "nbSoeurs": 0,
    "nbFreres": 0
  }'
```

### Exemple 2: Défunte féminine avec parents

```bash
curl -X POST http://localhost:8080/api/v1/heritage/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "sexeDefunt": "F",
    "conjointVivant": false,
    "pereVivant": true,
    "mereVivante": true,
    "nbFilles": 0,
    "nbGarcons": 0,
    "nbSoeurs": 2,
    "nbFreres": 1
  }'
```

### Exemple 3: JavaScript/Fetch

```javascript
const calculateHeritage = async (familyData) => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/heritage/calculate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(familyData)
    });

    if (!response.ok) {
      const error = await response.json();
      console.error('Erreur:', error.message);
      return;
    }

    const result = await response.json();
    console.log('Résultat du calcul:', result);
    return result;
  } catch (error) {
    console.error('Erreur réseau:', error);
  }
};

// Utilisation
calculateHeritage({
  sexeDefunt: 'M',
  conjointVivant: true,
  pereVivant: false,
  mereVivante: false,
  nbFilles: 1,
  nbGarcons: 1,
  nbSoeurs: 0,
  nbFreres: 0
});
```

## 🔒 CORS

L'API accepte les requêtes depuis les origines suivantes par défaut:
- `http://localhost:4200` (Angular)
- `http://localhost:3000` (React)
- `http://localhost:8080` (même origine)

Méthodes autorisées: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`

Configuration personnalisable via `application.properties`:

```properties
cors.allowed-origins=http://localhost:4200,http://localhost:3000
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
```

## 📈 Monitoring

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Métriques

```bash
curl http://localhost:8080/actuator/metrics
```

### Informations

```bash
curl http://localhost:8080/actuator/info
```

## 📋 Règles de calcul

L'API implémente les règles suivantes:

### Conjoint
- **Défunt masculin**: 1/8 si enfants, 1/4 sinon
- **Défunte féminine**: 1/4 si enfants, 1/2 sinon

### Parents
- **Père**: 1/6 si enfants ou conjoint, 2/3 sinon
- **Mère**: 1/6 si enfants/conjoint/fratrie, 1/3 sinon (1/6 avec fratrie)

### Enfants
- **Fille unique**: 1/2 du restant
- **Plusieurs filles**: 2/3 du restant (divisé équitablement)
- **Avec garçons**: Part du garçon = 2 × Part de la fille

### Fratrie
- Hérite uniquement s'il reste une part après les héritiers prioritaires
- Mêmes règles que les enfants

## 🐛 Support & Contact

- **Email**: contact@frida-heritage.com
- **Documentation**: https://frida-heritage.com/docs
- **Issues**: https://github.com/votre-org/frida-calculs-api/issues

## 📄 Licence

MIT License - Voir le fichier LICENSE pour plus de détails.
