export interface Fraction {
    numerateur: number;
    denominateur: number;
}

export interface Heritier {
    heritier: string;
    baseCalcul?: string;
    part: Fraction;
    partIrreductible?: Fraction;
    partLegale?: Fraction;
    cadreLegal?: string;
}

export interface TombeDetail {
    identifiant: string;
    sexeParentPredecede: string;
    lienParente: string;
    partSimulee: Fraction;
    wasiyyaEffective: Fraction;
    plafonnee: boolean;
    beneficiaires: Heritier[];
}

export interface HeritageResponse {
    timestamp: string;
    calculId: string;
    composition: any;
    heritiers: Heritier[];
    nombreHeritiers: number;
    denominateurCommun: number;
    message: string;
    calculComplet: boolean;
    partRestante?: Fraction;
    grandMerePaternelleVivante: boolean;

    // Champs multi-tombes (mode étendu uniquement)
    detailTombes?: TombeDetail[];
    nombreTombes?: number;
}
