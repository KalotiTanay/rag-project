import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ConversationService } from '../../core/services/conversation.service';
import { ChatService } from '../../core/services/chat.service';
import { Citation, ConversationSummary, MessageResponse } from '../../core/models/api.models';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  citations: Citation[];
  streaming?: boolean;
}

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="chat-layout">
      <aside class="sidebar">
        <button class="new-chat-btn" (click)="startNewConversation()">+ New chat</button>
        <div class="conversation-list">
          @for (conv of conversations; track conv.id) {
            <a [routerLink]="['/chat', conv.id]"
               class="conversation-item"
               [class.active]="conv.id === activeConversationId">
              {{ formatConversationLabel(conv) }}
            </a>
          }
        </div>
      </aside>

      <section class="chat-main">
        <div class="messages">
          @for (msg of messages; track $index) {
            <div class="message" [class]="'message-' + msg.role">
              <div class="bubble">
                <p>{{ msg.content }}@if (msg.streaming) {<span class="cursor">▍</span>}</p>
                @if (msg.citations.length > 0) {
                  <div class="citations">
                    @for (c of msg.citations; track c.chunkId) {
                      <span class="citation-chip">
                        {{ c.documentFilename }}@if (c.pageNumber) {, p.{{ c.pageNumber }}}
                      </span>
                    }
                  </div>
                }
              </div>
            </div>
          }
          @if (messages.length === 0) {
            <p class="empty-state">Ask a question about your uploaded documents to get started.</p>
          }
        </div>

        <form class="composer" (ngSubmit)="sendMessage()">
          <input
            [(ngModel)]="draftQuestion"
            name="question"
            placeholder="Ask a question about your documents..."
            [disabled]="isStreaming"
            autocomplete="off">
          <button type="submit" [disabled]="isStreaming || !draftQuestion.trim()">Send</button>
        </form>
      </section>
    </div>
  `,
  styles: [`
    .chat-layout { display: flex; height: 100%; }
    .sidebar {
      width: 220px; background: #fafafa; border-right: 1px solid #e3e3e3;
      display: flex; flex-direction: column; padding: 1rem;
    }
    .new-chat-btn {
      background: #1a1a1a; color: white; border: none; border-radius: 6px;
      padding: 0.6rem; margin-bottom: 1rem; font-size: 0.9rem;
    }
    .conversation-list { display: flex; flex-direction: column; gap: 0.25rem; overflow-y: auto; }
    .conversation-item {
      padding: 0.5rem; border-radius: 6px; text-decoration: none; color: #333;
      font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
    }
    .conversation-item.active, .conversation-item:hover { background: #e9e9ff; }

    .chat-main { flex: 1; display: flex; flex-direction: column; min-width: 0; }
    .messages { flex: 1; overflow-y: auto; padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; }
    .empty-state { color: #888; text-align: center; margin-top: 3rem; }
    .message { display: flex; }
    .message-user { justify-content: flex-end; }
    .message-assistant { justify-content: flex-start; }
    .bubble {
      max-width: 70%; padding: 0.75rem 1rem; border-radius: 12px; line-height: 1.4;
    }
    .message-user .bubble { background: #1a1a1a; color: white; }
    .message-assistant .bubble { background: white; border: 1px solid #e3e3e3; }
    .bubble p { margin: 0; white-space: pre-wrap; }
    .cursor { animation: blink 1s step-start infinite; }
    @keyframes blink { 50% { opacity: 0; } }
    .citations { margin-top: 0.5rem; display: flex; flex-wrap: wrap; gap: 0.35rem; }
    .citation-chip {
      font-size: 0.7rem; background: #eef3ff; color: #2b4fbf; padding: 0.15rem 0.5rem;
      border-radius: 10px;
    }
    .composer { display: flex; gap: 0.5rem; padding: 1rem 1.5rem; border-top: 1px solid #e3e3e3; background: white; }
    .composer input {
      flex: 1; padding: 0.65rem 0.9rem; border-radius: 20px; border: 1px solid #ddd; font-size: 0.95rem;
    }
    .composer button {
      background: #1a1a1a; color: white; border: none; border-radius: 20px; padding: 0.6rem 1.25rem;
    }
    .composer button:disabled { opacity: 0.5; cursor: default; }
  `],
})
export class ChatPageComponent implements OnInit {
  conversations: ConversationSummary[] = [];
  messages: ChatMessage[] = [];
  activeConversationId: string | null = null;
  draftQuestion = '';
  isStreaming = false;

  constructor(
    private conversationService: ConversationService,
    private chatService: ChatService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadConversations();
    this.route.paramMap.subscribe((params) => {
      const id = params.get('conversationId');
      this.activeConversationId = id;
      this.messages = [];
      if (id) {
        this.loadHistory(id);
      }
    });
  }

  startNewConversation(): void {
    this.router.navigate(['/chat']);
  }

  formatConversationLabel(conv: ConversationSummary): string {
    return new Date(conv.createdAt).toLocaleString();
  }

  private loadConversations(): void {
    this.conversationService.list().subscribe({
      next: (list) => (this.conversations = list),
      error: (err) => console.error('Failed to load conversations', err),
    });
  }

  private loadHistory(conversationId: string): void {
    this.conversationService.getMessages(conversationId).subscribe({
      next: (history: MessageResponse[]) => {
        this.messages = history.map((m) => ({
          role: m.role,
          content: m.content,

          citations: [],
        }));
      },
      error: (err) => console.error('Failed to load conversation history', err),
    });
  }

  sendMessage(): void {
    const question = this.draftQuestion.trim();
    if (!question || this.isStreaming) return;

    this.messages.push({ role: 'user', content: question, citations: [] });
    const assistantMessage: ChatMessage = { role: 'assistant', content: '', citations: [], streaming: true };
    this.messages.push(assistantMessage);

    this.draftQuestion = '';
    this.isStreaming = true;

    this.chatService.streamAnswer(question, this.activeConversationId, {
      onToken: (token) => {
        assistantMessage.content += token;
      },
      onDone: (payload) => {
        assistantMessage.citations = payload.citations;
        assistantMessage.streaming = false;
        this.isStreaming = false;

        const isNewConversation = !this.activeConversationId;
        this.activeConversationId = payload.conversationId;
        if (isNewConversation) {
          this.router.navigate(['/chat', payload.conversationId]);
          this.loadConversations();
        }
      },
      onError: (err) => {
        console.error('Streaming failed', err);
        assistantMessage.content ||= 'Something went wrong generating a response. Please try again.';
        assistantMessage.streaming = false;
        this.isStreaming = false;
      },
    });
  }
}
