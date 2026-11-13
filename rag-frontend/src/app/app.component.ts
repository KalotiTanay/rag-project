import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
      <header class="topbar">
        <span class="brand">RAG Document Q&amp;A</span>
        <nav>
          <a routerLink="/documents" routerLinkActive="active">Documents</a>
          <a routerLink="/chat" routerLinkActive="active">Chat</a>
        </nav>
      </header>
      <main class="content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .shell { display: flex; flex-direction: column; height: 100vh; }
    .topbar {
      display: flex; align-items: center; justify-content: space-between;
      padding: 0.75rem 1.5rem; background: #1a1a1a; color: white;
    }
    .brand { font-weight: 600; }
    nav a {
      color: #cfcfcf; text-decoration: none; margin-left: 1.5rem; font-size: 0.9rem;
    }
    nav a.active, nav a:hover { color: white; }
    .content { flex: 1; overflow: hidden; }
  `],
})
export class AppComponent {}
