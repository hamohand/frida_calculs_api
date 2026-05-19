import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FamilyRequest } from '../models/family-request.model';
import { HeritageResponse } from '../models/heritage-response.model';

@Injectable({
    providedIn: 'root'
})
export class CalculationService {

    private apiUrl = 'http://localhost:8081/calculs/api/v1/heritage/calculate';

    constructor(private http: HttpClient) { }

    calculate(request: FamilyRequest): Observable<HeritageResponse> {
        return this.http.post<HeritageResponse>(this.apiUrl, request, {
            headers: { 'Content-Type': 'application/json' }
        });
    }
}
