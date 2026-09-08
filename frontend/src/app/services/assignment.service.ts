import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Assignment } from '../models/assignment.model';

@Injectable({ providedIn: 'root' })
export class AssignmentService {
  private http = inject(HttpClient);
  private readonly baseUrl = '/api/assignments';

  getAll(): Observable<Assignment[]> {
    return this.http.get<Assignment[]>(this.baseUrl);
  }

  assign(assignment: {
    assetId: number;
    assignedTo: string;
    notes: string;
  }): Observable<Assignment> {
    return this.http.post<Assignment>(this.baseUrl, assignment);
  }

  return(id: number): Observable<Assignment> {
    return this.http.post<Assignment>(`${this.baseUrl}/${id}/return`, {});
  }
}
