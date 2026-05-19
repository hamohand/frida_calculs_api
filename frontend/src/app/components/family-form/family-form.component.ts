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
        nbFreres: 0
    };

    result: HeritageResponse | null = null;
    error: string | null = null;
    loading: boolean = false;

    constructor(private calculationService: CalculationService, private cdr: ChangeDetectorRef) { }

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
                // Affiche l'erreur complète pour le débogage
                this.error = `Erreur: ${err.status} - ${err.message}. Détails: ${JSON.stringify(err.error)}`;
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
}
