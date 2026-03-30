import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import { useAuthStore } from '@/stores/authStore';

interface UseWebSocketOptions {
  topic: string;
  onMessage: (data: unknown) => void;
  enabled?: boolean;
}

export function useWebSocket({ topic, onMessage, enabled = true }: UseWebSocketOptions) {
  const clientRef = useRef<Client | null>(null);
  const { accessToken } = useAuthStore();

  const connect = useCallback(() => {
    if (!enabled || !accessToken) return;

    const client = new Client({
      brokerURL: `${import.meta.env.VITE_WS_URL?.replace('http', 'ws')}/ws`,
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      onConnect: () => {
        client.subscribe(topic, (message) => {
          onMessage(JSON.parse(message.body));
        });
      },
      reconnectDelay: 5000,
    });

    client.activate();
    clientRef.current = client;
  }, [topic, onMessage, enabled, accessToken]);

  useEffect(() => {
    connect();
    return () => {
      clientRef.current?.deactivate();
    };
  }, [connect]);

  const disconnect = useCallback(() => {
    clientRef.current?.deactivate();
  }, []);

  return { disconnect };
}
