import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CalculationService } from '../../services/calculation.service';
import { FamilyRequest, ExtendedFamilyRequest, Tombe } from '../../models/family-request.model';
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
        grandPerePaternelVivant: false,
        grandMerePaternelleVivante: false,
        nbFilles: 0,
        nbGarcons: 0,
        nbSoeurs: 0,
        nbFreres: 0,
        nbOncles: 0,
        nbCousins: 0,
        nbPetitsFils: 0,
        nbPetitesFilles: 0,
        sexeParentPredecede: 'M'
    };

    result: HeritageResponse | null = null;
    error: string | null = null;
    loading: boolean = false;
    table2Rows: any[] = [];

    // Mode étendu multi-tombes
    modeEtendu: boolean = false;
    tombes: Tombe[] = [];

    constructor(private calculationService: CalculationService, private cdr: ChangeDetectorRef) { }

    isSiblingsExcluded(): boolean {
        return !!this.request.pereVivant || 
               !!this.request.grandPerePaternelVivant || 
               (this.request.nbGarcons !== null && this.request.nbGarcons > 0);
    }

    isOnclesExcluded(): boolean {
        return !!this.request.pereVivant || 
               !!this.request.grandPerePaternelVivant ||
               (this.request.nbGarcons !== null && this.request.nbGarcons > 0) ||
               (this.request.nbFreres !== null && this.request.nbFreres > 0);
    }

    isCousinsExcluded(): boolean {
        return this.isOnclesExcluded() || 
               (this.request.nbOncles !== null && this.request.nbOncles > 0);
    }

    checkExclusions() {
        if (this.request.pereVivant) {
            this.request.grandPerePaternelVivant = false;
            this.request.grandMerePaternelleVivante = false;
        }
        if (this.request.mereVivante) {
            this.request.grandMerePaternelleVivante = false;
        }
        if (this.isSiblingsExcluded()) {
            this.request.nbSoeurs = 0;
            this.request.nbFreres = 0;
            if (this.modeEtendu) {
                this.tombes.forEach(t => {
                    if (t.lienParente === 'frere_soeur') {
                        t.lienParente = 'enfant';
                        t.nbDescendantsMales = 0;
                        t.nbDescendantesFemelles = 0;
                    }
                });
            }
        }
        if (this.isOnclesExcluded()) {
            this.request.nbOncles = 0;
        }
        if (this.isCousinsExcluded()) {
            this.request.nbCousins = 0;
        }
    }

    isValidRequest(): boolean {
        const req = this.request;
        if (!req.sexeDefunt) return false;
        
        const hasHeir = (req.nbConjoints > 0) || req.pereVivant || req.mereVivante || 
                        req.grandPerePaternelVivant || req.grandMerePaternelleVivante || (req.nbFilles > 0) || (req.nbGarcons > 0) || 
                        (req.nbSoeurs > 0) || (req.nbFreres > 0) || (req.nbOncles > 0) || (req.nbCousins > 0) ||
                        (req.nbPetitsFils !== undefined && req.nbPetitsFils > 0) || 
                        (req.nbPetitesFilles !== undefined && req.nbPetitesFilles > 0);
                        
        return hasHeir;
    }

    onFieldChange() {
        this.checkExclusions();
        this.onSubmit();
    }

    onModeChange() {
        if (this.modeEtendu && this.tombes.length === 0) {
            this.addTombe();
        }
        // Effacer les résultats pour recalculer
        this.result = null;
        this.table2Rows = [];
        this.onSubmit();
    }

    addTombe() {
        this.tombes.push({
            identifiant: 'tombe_' + (this.tombes.length + 1),
            sexeParentPredecede: 'M',
            lienParente: 'enfant',
            nbDescendantsMales: 0,
            nbDescendantesFemelles: 0
        });
    }

    removeTombe(index: number) {
        this.tombes.splice(index, 1);
        // Renuméroter
        this.tombes.forEach((t, i) => t.identifiant = 'tombe_' + (i + 1));
        this.onFieldChange();
    }

    sortHeritiers(heritiers: Heritier[]): Heritier[] {
        if (!heritiers) return [];
        return [...heritiers].sort((a, b) => {
            const isTotalA = a.baseCalcul === "de la totalité de l'héritage";
            const isTotalB = b.baseCalcul === "de la totalité de l'héritage";
            
            if (isTotalA && !isTotalB) return -1;
            if (!isTotalA && isTotalB) return 1;
            
            return 0;
        });
    }

    updateTable2Rows() {
        if (!this.result || !this.result.heritiers) {
            this.table2Rows = [];
            return;
        }

        const rows: any[] = [];
        
        for (const item of this.result.heritiers) {
            if (item.heritier !== 'part restant' && item.heritier !== 'fille' && item.heritier !== 'garçon') {
                rows.push({
                    label: item.heritier,
                    partLegale: this.getLegalFractionDisplay(item),
                    baseCalcul: item.baseCalcul || '-',
                    cadreLegal: item.cadreLegal || '-'
                });
            }
        }

        if (this.hasChildren()) {
            rows.push({
                label: this.getChildrenLabel(),
                partLegale: this.getChildrenLegalShare(),
                baseCalcul: this.getChildrenBaseCalcul(),
                cadreLegal: this.getChildrenCadreLegal()
            });
        }

        this.table2Rows = rows.sort((a, b) => {
            const isTotalA = a.baseCalcul === "de la totalité de l'héritage";
            const isTotalB = b.baseCalcul === "de la totalité de l'héritage";
            
            if (isTotalA && !isTotalB) return -1;
            if (!isTotalA && isTotalB) return 1;
            
            return 0;
        });
    }

    onSubmit() {
        this.error = null;
        
        this.request.nbConjoints = Number(this.request.nbConjoints);
        this.request.nbFilles = Number(this.request.nbFilles);
        this.request.nbGarcons = Number(this.request.nbGarcons);
        this.request.nbSoeurs = Number(this.request.nbSoeurs);
        this.request.nbFreres = Number(this.request.nbFreres);
        this.request.nbOncles = Number(this.request.nbOncles);
        this.request.nbCousins = Number(this.request.nbCousins);

        if (!this.isValidRequest()) {
            this.result = null;
            this.table2Rows = [];
            return;
        }

        this.loading = true;

        if (this.modeEtendu) {
            this.onSubmitExtended();
            return;
        }

        console.log('Envoi de la demande (simple):', this.request);

        this.calculationService.calculate(this.request).subscribe({
            next: (response) => this.handleResponse(response),
            error: (err) => this.handleError(err)
        });
    }

    onSubmitExtended() {
        const extRequest: ExtendedFamilyRequest = {
            ...this.request,
            tombes: this.tombes
        };

        // En mode étendu, on ne passe pas les champs simples de wasiyya
        delete (extRequest as any).nbPetitsFils;
        delete (extRequest as any).nbPetitesFilles;
        delete (extRequest as any).sexeParentPredecede;

        console.log('Envoi de la demande (étendu):', extRequest);

        this.calculationService.calculateExtended(extRequest).subscribe({
            next: (response) => this.handleResponse(response),
            error: (err) => this.handleError(err)
        });
    }

    private handleResponse(response: HeritageResponse) {
        console.log('Réponse reçue:', response);
        try {
            this.result = response;
            if (this.result && this.result.heritiers) {
                this.result.heritiers = this.sortHeritiers(this.result.heritiers);
            }
            this.updateTable2Rows();
            this.loading = false;
            this.cdr.detectChanges();
        } catch (e) {
            console.error('Erreur affichage:', e);
            this.error = 'Erreur lors de l\'affichage des résultats';
            this.loading = false;
        }
    }

    private handleError(err: any) {
        console.error('Erreur API:', err);
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

    getHeirLabel(item: Heritier): string {
        if (!item || !this.result || !this.result.composition) return item.heritier;
        const comp = this.result.composition;
        
        switch (item.heritier) {
            case 'conjoint':
                return comp.nbConjoints > 1 ? 'conjoint (chacun)' : 'conjoint';
            case 'fille':
                return comp.nbFilles > 1 ? 'fille (chacune)' : 'fille';
            case 'garçon':
                return comp.nbGarcons > 1 ? 'garçon (chacun)' : 'garçon';
            case 'soeur':
                return comp.nbSoeurs > 1 ? 'soeur (chacune)' : 'soeur';
            case 'frere':
                return comp.nbFreres > 1 ? 'frere (chacun)' : 'frere';
            case 'oncle paternel':
                return comp.nbOncles > 1 ? 'oncle paternel (chacun)' : 'oncle paternel';
            case 'cousin paternel':
                return comp.nbCousins > 1 ? 'cousin paternel (chacun)' : 'cousin paternel';
            case 'petit-fils':
                return (comp.nbPetitsFils && comp.nbPetitsFils > 1) ? 'petit-fils (chacun)' : 'petit-fils';
            case 'petite-fille':
                return (comp.nbPetitesFilles && comp.nbPetitesFilles > 1) ? 'petite-fille (chacune)' : 'petite-fille';
            default:
                return item.heritier;
        }
    }
}
