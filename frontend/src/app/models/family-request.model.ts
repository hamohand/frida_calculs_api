export interface Tombe {
    identifiant: string;
    sexeParentPredecede: 'M' | 'F';
    lienParente: 'enfant' | 'frere_soeur';
    nbDescendantsMales: number;
    nbDescendantesFemelles: number;
}

export interface FamilyRequest {
    sexeDefunt: 'M' | 'F';
    nbConjoints: number;
    pereVivant: boolean;
    mereVivante: boolean;
    grandPerePaternelVivant: boolean;
    grandMerePaternelleVivante: boolean;
    nbFilles: number;
    nbGarcons: number;
    nbSoeurs: number;
    nbFreres: number;
    nbOncles: number;
    nbCousins: number;

    // Champs pour le testament obligatoire simple (Wasiyya Wajiba)
    nbPetitsFils?: number;
    nbPetitesFilles?: number;
    sexeParentPredecede?: string;
}

export interface ExtendedFamilyRequest extends FamilyRequest {
    tombes: Tombe[];
}
