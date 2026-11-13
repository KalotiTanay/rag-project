import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { StreamDonePayload } from '../models/api.models';

export interface StreamHandlers {
  onToken: (token: string) => void;
  onDone: (payload: StreamDonePayload) => void;
  onError: (error: unknown) => void;
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly streamUrl = `${environment.apiBaseUrl}/query/stream`;

  streamAnswer(question: string, conversationId: string | null, handlers: StreamHandlers): AbortController {
    const controller = new AbortController();

    fetch(this.streamUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'text/event-stream' },
      body: JSON.stringify({ question, conversationId }),
      signal: controller.signal,
    })
      .then((response) => {
        if (!response.ok || !response.body) {
          throw new Error(`Stream request failed with status ${response.status}`);
        }
        return this.readStream(response.body, handlers);
      })
      .catch((err) => {

        if ((err as Error)?.name !== 'AbortError') {
          handlers.onError(err);
        }
      });

    return controller;
  }

  private async readStream(body: ReadableStream<Uint8Array>, handlers: StreamHandlers): Promise<void> {
    const reader = body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });

      let boundary: number;
      while ((boundary = buffer.indexOf('\n\n')) !== -1) {
        const rawEvent = buffer.slice(0, boundary);
        buffer = buffer.slice(boundary + 2);
        this.dispatchEvent(rawEvent, handlers);
      }
    }
  }

  private dispatchEvent(rawEvent: string, handlers: StreamHandlers): void {
    let eventName = 'message';
    let dataLines: string[] = [];

    for (const line of rawEvent.split('\n')) {
      if (line.startsWith('event:')) {
        eventName = line.slice('event:'.length).trim();
      } else if (line.startsWith('data:')) {
        dataLines.push(line.slice('data:'.length).trim());
      }
    }

    const data = dataLines.join('\n');
    if (!data) return;

    if (eventName === 'token') {

      handlers.onToken(data);
    } else if (eventName === 'done') {
      handlers.onDone(JSON.parse(data) as StreamDonePayload);
    }
  }

}
