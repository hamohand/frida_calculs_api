# Rapport d'Analyse : Application de Calcul d'Héritage (Frida)

Ce document présente une analyse détaillée de l'application de calcul des parts d'héritage selon la loi islamique.

## 1. Architecture Globale
L'application est structurée comme une API REST Spring Boot classique :
- **Contrôleur** : `CalculsPartsController` gère les requêtes entrantes.
- **Service** : `CalculPartsService` orchestre le processus de calcul.
- **Modèle Métier** : `Heritiers` contient les règles spécifiques de calcul pour chaque type d'héritier.
- **Utilitaires** : `Fraction` gère les opérations mathématiques sur les parts (réduction au même dénominateur, simplification).

## 2. Processus de Calcul
Le calcul suit une séquence logique définie dans `CalculPartsService.calculParts` :

1.  **Initialisation** : La part totale est de 1/1.
2.  **Distribution séquentielle** : Les parts sont distribuées dans un ordre précis :
    - **Conjoint** (Époux/Épouse)
    - **Parents** (Père et Mère)
    - **Enfants** (Filles et Garçons)
    - **Fratrie** (Frères et Sœurs)
3.  **Part Restante** : Toute part non distribuée est isolée comme "part restant".
4.  **Normalisation** : Toutes les parts sont réduites au même dénominateur pour faciliter la lecture (ex: tous sur 24 ou 48).

## 3. Règles Métier Clés (`Heritiers.java`)

### Conjoint
- **Époux (Défunt = Femme)** : 1/2 si pas d'enfants, 1/4 si enfants.
- **Épouse (Défunt = Homme)** : 1/4 si pas d'enfants, 1/8 si enfants.

### Parents
- **Mère** : 1/6 s'il y a des enfants ou des frères/sœurs. 1/3 sinon.
- **Père** : 1/6 s'il y a des enfants ou un conjoint. 2/3 sous certaines conditions (résiduelle).

### Enfants
- **Fille unique** : 1/2 (du reste).
- **Plusieurs filles (sans garçons)** : 2/3 à partager également.
- **Mixte (Garçons + Filles)** : Règle du "double pour le mâle".

## 4. Points Forts et Observations
- **Précision Mathématique** : L'utilisation d'une classe `Fraction` dédiée évite les problèmes d'arrondis liés aux nombres flottants.
- **Extensibilité** : La structure permet d'ajouter facilement de nouveaux types d'héritiers ou des règles spécifiques.
- **Documentation API** : Utilisation de Swagger/OpenAPI pour documenter les modèles.

## 5. Modèles de Données
- **Entrée (`FamilyRequest`)** : Sexe du défunt et indicateurs de présence des héritiers.
- **Sortie (`HeritageResponse`)** : Liste détaillée des héritiers avec leurs fractions simplifiées et normalisées.
