import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FamilyRequest, ExtendedFamilyRequest } from '../models/family-request.model';
import { HeritageResponse } from '../models/heritage-response.model';

@Injectable({
    providedIn: 'root'
})
export class CalculationService {

    private apiUrl = '/calculs/api/v1/heritage/calculate';
    private apiUrlExtended = '/calculs/api/v1/heritage/calculate-extended';

    constructor(private http: HttpClient) { }

    calculate(request: FamilyRequest): Observable<HeritageResponse> {
        return this.http.post<HeritageResponse>(this.apiUrl, request, {
            headers: { 'Content-Type': 'application/json' }
        });
    }

    calculateExtended(request: ExtendedFamilyRequest): Observable<HeritageResponse> {
        return this.http.post<HeritageResponse>(this.apiUrlExtended, request, {
            headers: { 'Content-Type': 'application/json' }
        });
    }
}
