import { Routes } from '@angular/router';
import { DocumentsPageComponent } from './features/documents/documents-page.component';
import { ChatPageComponent } from './features/chat/chat-page.component';

export const routes: Routes = [
  { path: '', redirectTo: 'documents', pathMatch: 'full' },
  { path: 'documents', component: DocumentsPageComponent },
  { path: 'chat', component: ChatPageComponent },
  { path: 'chat/:conversationId', component: ChatPageComponent },
  { path: '**', redirectTo: 'documents' },
];
