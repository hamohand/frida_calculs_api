export interface FamilyRequest {
    sexeDefunt: 'M' | 'F';
    nbConjoints: number;
    pereVivant: boolean;
    mereVivante: boolean;
    grandPerePaternelVivant: boolean;
    nbFilles: number;
    nbGarcons: number;
    nbSoeurs: number;
    nbFreres: number;
    nbOncles: number;
    nbCousins: number;

    // Champs pour le testament obligatoire (Wasiyya Wajiba)
    nbPetitsFils?: number;
    nbPetitesFilles?: number;
    sexeParentPredecede?: string;
}
