export type DocumentStatus = 'PROCESSING' | 'READY' | 'FAILED';

export interface DocumentResponse {
  id: string;
  filename: string;
  status: DocumentStatus;
  failureReason: string | null;
  uploadDate: string;
}

export interface Citation {
  chunkId: string;
  documentFilename: string;
  pageNumber: number | null;
}

export interface QueryResponse {
  conversationId: string;
  answer: string;
  citations: Citation[];
}

export interface StreamDonePayload {
  conversationId: string;
  citations: Citation[];
}

export interface ConversationSummary {
  id: string;
  createdAt: string;
}

export interface MessageResponse {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  citedChunkIds: string[];
  createdAt: string;
}
