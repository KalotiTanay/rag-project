import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DocumentResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly baseUrl = `${environment.apiBaseUrl}/documents`;

  constructor(private http: HttpClient) {}

  list(): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(this.baseUrl);
  }

  get(id: string): Observable<DocumentResponse> {
    return this.http.get<DocumentResponse>(`${this.baseUrl}/${id}`);
  }

  upload(file: File): Observable<DocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DocumentResponse>(`${this.baseUrl}/upload`, formData);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
