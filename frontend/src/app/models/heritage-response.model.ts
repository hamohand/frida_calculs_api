export interface Fraction {
    numerateur: number;
    denominateur: number;
}

export interface Heritier {
    heritier: string; // Ensure this matches Java field name
    part: Fraction;
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
}
