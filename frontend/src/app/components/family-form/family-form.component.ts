import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CalculationService } from '../../services/calculation.service';
import { FamilyRequest } from '../../models/family-request.model';
import { HeritageResponse, Heritier } from '../../models/heritage-response.model';

@Component({
    selector: 'app-family-form',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './family-form.component.html',
    styleUrls: ['./family-form.component.css']
})
export class FamilyFormComponent {

    request: FamilyRequest = {
        sexeDefunt: 'M',
        nbConjoints: 0,
        pereVivant: false,
        mereVivante: false,
        nbFilles: 0,
        nbGarcons: 0,
        nbSoeurs: 0,
        nbFreres: 0,
        nbOncles: 0,
        nbCousins: 0
    };

    result: HeritageResponse | null = null;
    error: string | null = null;
    loading: boolean = false;

    constructor(private calculationService: CalculationService, private cdr: ChangeDetectorRef) { }

    isSiblingsExcluded(): boolean {
        return !!this.request.pereVivant || (this.request.nbGarcons !== null && this.request.nbGarcons > 0);
    }

    isOnclesExcluded(): boolean {
        return !!this.request.pereVivant || 
               (this.request.nbGarcons !== null && this.request.nbGarcons > 0) ||
               (this.request.nbFreres !== null && this.request.nbFreres > 0);
    }

    isCousinsExcluded(): boolean {
        return this.isOnclesExcluded() || 
               (this.request.nbOncles !== null && this.request.nbOncles > 0);
    }

    checkExclusions() {
        if (this.isSiblingsExcluded()) {
            this.request.nbSoeurs = 0;
            this.request.nbFreres = 0;
        }
        if (this.isOnclesExcluded()) {
            this.request.nbOncles = 0;
        }
        if (this.isCousinsExcluded()) {
            this.request.nbCousins = 0;
        }
    }

    onSubmit() {
        this.loading = true;
        this.error = null;
        this.result = null;

        // Conversion des inputs number
        this.request.nbConjoints = Number(this.request.nbConjoints);
        this.request.nbFilles = Number(this.request.nbFilles);
        this.request.nbGarcons = Number(this.request.nbGarcons);
        this.request.nbSoeurs = Number(this.request.nbSoeurs);
        this.request.nbFreres = Number(this.request.nbFreres);
        this.request.nbOncles = Number(this.request.nbOncles);
        this.request.nbCousins = Number(this.request.nbCousins);

        console.log('Envoi de la demande:', this.request);

        this.calculationService.calculate(this.request).subscribe({
            next: (response) => {
                console.log('Réponse reçue:', response);
                try {
                    this.result = response;
                    this.loading = false;
                    this.cdr.detectChanges(); // Force UI update
                } catch (e) {
                    console.error('Erreur affichage:', e);
                    this.error = 'Erreur lors de l\'affichage des résultats';
                    this.loading = false;
                }
            },
            error: (err) => {
                console.error('Erreur API:', err);
                // Sécurisation de l'affichage de l'erreur
                let errorDetails = '';
                try {
                    errorDetails = (err.error && typeof err.error === 'object') ? JSON.stringify(err.error) : err.error;
                } catch (e) {
                    errorDetails = 'Erreur réseau ou objet non serialisable';
                }
                this.error = `Erreur: ${err.status} - ${err.message}. Détails: ${errorDetails}`;
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    getFractionDisplay(heritier: Heritier): string {
        if (!heritier || !heritier.part) return 'N/A';
        const { numerateur, denominateur } = heritier.part;
        if (numerateur === 0) return '0';
        if (numerateur === denominateur) return '1';
        return `${numerateur}/${denominateur}`;
    }

    getIrreducibleFractionDisplay(heritier: Heritier): string {
        if (!heritier || !heritier.partIrreductible) return 'N/A';
        const { numerateur, denominateur } = heritier.partIrreductible;
        if (numerateur === 0) return '0';
        if (numerateur === denominateur) return '1';
        return `${numerateur}/${denominateur}`;
    }

    getLegalFractionDisplay(heritier: Heritier): string {
        if (!heritier || !heritier.partLegale) return 'N/A';
        const { numerateur, denominateur } = heritier.partLegale;
        if (numerateur === 0) return '0';
        if (numerateur === denominateur) return '1';
        return `${numerateur}/${denominateur}`;
    }

    hasChildren(): boolean {
        if (!this.result || !this.result.composition) return false;
        const nbFilles = this.result.composition.nbFilles || 0;
        const nbGarcons = this.result.composition.nbGarcons || 0;
        return nbFilles > 0 || nbGarcons > 0;
    }

    getChildrenLabel(): string {
        if (!this.result || !this.result.composition) return 'Enfants';
        const nbFilles = this.result.composition.nbFilles || 0;
        const nbGarcons = this.result.composition.nbGarcons || 0;
        if (nbFilles > 0 && nbGarcons > 0) {
            return `Enfants (${nbGarcons} garçon(s) et ${nbFilles} fille(s))`;
        } else if (nbGarcons > 0) {
            return `Enfants (${nbGarcons} garçon(s))`;
        } else if (nbFilles > 0) {
            return `Enfants (${nbFilles} fille(s))`;
        }
        return 'Enfants';
    }

    getChildrenLegalShare(): string {
        if (!this.result || !this.result.composition) return '-';
        const nbFilles = this.result.composition.nbFilles || 0;
        const nbGarcons = this.result.composition.nbGarcons || 0;
        if (nbGarcons > 0 && nbFilles > 0) {
            return 'Reste (Asaba) - Ratio 2:1 (le double pour le garçon)';
        } else if (nbGarcons > 0) {
            return 'Reste (Asaba)';
        } else if (nbFilles > 0) {
            return nbFilles === 1 ? '1/2' : '2/3 (à partager)';
        }
        return '-';
    }

    getChildrenBaseCalcul(): string {
        if (!this.result || !this.result.composition) return '-';
        const nbFilles = this.result.composition.nbFilles || 0;
        const nbGarcons = this.result.composition.nbGarcons || 0;
        if (nbGarcons > 0) {
            return "du reste de l'héritage";
        } else if (nbFilles > 0) {
            return "de la totalité de l'héritage";
        }
        return '-';
    }

    getChildrenCadreLegal(): string {
        if (!this.result || !this.result.heritiers) return '-';
        
        // Trouver d'abord si le garçon est présent
        const garcon = this.result.heritiers.find(h => h.heritier === 'garçon');
        if (garcon) {
            return garcon.cadreLegal || 'العصبة';
        }
        
        // Sinon la fille
        const fille = this.result.heritiers.find(h => h.heritier === 'fille');
        if (fille) {
            return fille.cadreLegal || 'الفرض';
        }
        
        return '-';
    }
}
