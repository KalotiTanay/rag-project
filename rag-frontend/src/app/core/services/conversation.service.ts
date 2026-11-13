import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ConversationSummary, MessageResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ConversationService {
  private readonly baseUrl = `${environment.apiBaseUrl}/conversations`;

  constructor(private http: HttpClient) {}

  list(): Observable<ConversationSummary[]> {
    return this.http.get<ConversationSummary[]>(this.baseUrl);
  }

  create(): Observable<ConversationSummary> {
    return this.http.post<ConversationSummary>(this.baseUrl, {});
  }

  getMessages(conversationId: string): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(`${this.baseUrl}/${conversationId}/messages`);
  }
}
