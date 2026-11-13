import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentService } from '../../core/services/document.service';
import { DocumentResponse } from '../../core/models/api.models';

@Component({
  selector: 'app-documents-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <section class="upload-zone"
               [class.dragover]="isDragOver"
               (dragover)="onDragOver($event)"
               (dragleave)="isDragOver = false"
               (drop)="onDrop($event)">
        <p>Drag &amp; drop a PDF, TXT, or MD file here</p>
        <p>or</p>
        <label class="file-picker">
          Choose file
          <input type="file" accept=".pdf,.txt,.md" (change)="onFileSelected($event)" hidden>
        </label>
        @if (uploadError) {
          <p class="error">{{ uploadError }}</p>
        }
      </section>

      <section class="doc-list">
        <div class="doc-list-header">
          <h2>Your Documents</h2>
          <button (click)="refresh()">Refresh</button>
        </div>

        @if (documents.length === 0) {
          <p class="empty">No documents uploaded yet.</p>
        }

        @for (doc of documents; track doc.id) {
          <div class="doc-row">
            <div class="doc-info">
              <span class="filename">{{ doc.filename }}</span>
              <span class="status" [class]="'status-' + doc.status.toLowerCase()">{{ doc.status }}</span>
            </div>
            @if (doc.status === 'FAILED' && doc.failureReason) {
              <div class="failure-reason">{{ doc.failureReason }}</div>
            }
            <button class="delete-btn" (click)="deleteDocument(doc.id)">Delete</button>
          </div>
        }
      </section>
    </div>
  `,
  styles: [`
    .page { padding: 2rem; max-width: 700px; margin: 0 auto; height: 100%; overflow-y: auto; }
    .upload-zone {
      border: 2px dashed #ccc; border-radius: 8px; padding: 2rem; text-align: center;
      margin-bottom: 2rem; transition: border-color 0.15s;
    }
    .upload-zone.dragover { border-color: #4a7dff; background: #eef3ff; }
    .file-picker {
      display: inline-block; background: #1a1a1a; color: white; padding: 0.5rem 1.25rem;
      border-radius: 6px; cursor: pointer; font-size: 0.9rem;
    }
    .error { color: #c0392b; margin-top: 0.75rem; }
    .doc-list-header { display: flex; justify-content: space-between; align-items: center; }
    .empty { color: #888; }
    .doc-row {
      display: flex; align-items: center; justify-content: space-between;
      padding: 0.75rem; border: 1px solid #e3e3e3; border-radius: 6px; margin-bottom: 0.5rem;
      background: white;
    }
    .doc-info { display: flex; flex-direction: column; gap: 0.15rem; }
    .filename { font-weight: 500; }
    .status { font-size: 0.75rem; width: fit-content; padding: 0.1rem 0.5rem; border-radius: 10px; }
    .status-processing { background: #fff3cd; color: #856404; }
    .status-ready { background: #d4edda; color: #155724; }
    .status-failed { background: #f8d7da; color: #721c24; }
    .failure-reason { font-size: 0.75rem; color: #c0392b; margin-top: 0.25rem; }
    .delete-btn { background: none; border: 1px solid #ddd; border-radius: 4px; padding: 0.3rem 0.75rem; }
    .delete-btn:hover { background: #f8d7da; border-color: #c0392b; }
  `],
})
export class DocumentsPageComponent implements OnInit, OnDestroy {
  documents: DocumentResponse[] = [];
  isDragOver = false;
  uploadError: string | null = null;

  private pollHandle: ReturnType<typeof setInterval> | null = null;

  constructor(private documentService: DocumentService) {}

  ngOnInit(): void {
    this.refresh();

    this.pollHandle = setInterval(() => this.refresh(), 4000);
  }

  ngOnDestroy(): void {
    if (this.pollHandle) clearInterval(this.pollHandle);
  }

  refresh(): void {
    this.documentService.list().subscribe({
      next: (docs) => (this.documents = docs),
      error: (err) => console.error('Failed to load documents', err),
    });
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
    const file = event.dataTransfer?.files?.[0];
    if (file) this.upload(file);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.upload(file);
    input.value = '';
  }

  deleteDocument(id: string): void {
    this.documentService.delete(id).subscribe({
      next: () => this.refresh(),
      error: (err) => console.error('Failed to delete document', err),
    });
  }

  private upload(file: File): void {
    this.uploadError = null;
    this.documentService.upload(file).subscribe({
      next: () => this.refresh(),
      error: (err) => {
        this.uploadError = err?.error?.message ?? 'Upload failed. Please try a different file.';
      },
    });
  }
}
