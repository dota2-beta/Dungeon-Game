import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class GameSocket {
    private client: Client;
    // Функция-коллбек, которую мы получим из React компонента (через onEvent)
    private eventHandler: ((event: any) => void) | null = null;

    constructor() {
        this.client = new Client({
            // URL твоего WebSocket Service
            webSocketFactory: () => new SockJS('http://localhost:9090/gs-websocket'),
            debug: (str) => console.log('[STOMP]: ' + str),
            reconnectDelay: 5000, // Автоматическое переподключение
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onWebSocketClose: () => {
                console.log("🔴 WebSocket Closed");
                if (this.eventHandler) {
                    this.eventHandler({ eventType: 'SOCKET_DISCONNECTED', payload: {} });
                }
            }
        });

        this.client.onConnect = (frame) => {
            console.log('✅ Connected: ' + frame);
            this.subscribeToUserErrors();
            this.subscribeToUserEvents();
            this.subscribeToStateSnapshot();
        };

        this.client.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        };

        
    }

    // Метод активации (вызывается в useEffect)
    public activate() {
        this.client.activate();
    }

    // Метод для регистрации слушателя из React (связь с Reducer)
    public onEvent(callback: (event: any) => void) {
        this.eventHandler = callback;
    }

    // --- Private Subscriptions (Автоматические) ---

    // Личные события (SessionCreated, Errors и т.д.)
    private subscribeToUserEvents() {
        this.client.subscribe('/user/queue/events', (message: IMessage) => {
            this.processMessage(message);
        });
    }

    // private subscribeToUserErrors() {
    //     this.client.subscribe('/user/queue/error', (message: IMessage) => {
    //         // Ошибки можно либо логировать, либо тоже кидать в Reducer, 
    //         // если хочешь показывать их в UI
    //         console.error('❌ STOMP ERROR:', message.body);
    //     });
    // }

    private subscribeToUserErrors() {
    this.client.subscribe('/user/queue/error', (message: IMessage) => {
        try {
            const error = JSON.parse(message.body);
            console.error('❌ SERVER ERROR:', error);
            
            if (this.eventHandler) {
                this.eventHandler({ 
                    eventType: 'error',
                    payload: error 
                });
            }
        } catch (e) {
            console.error('Raw Error:', message.body);
        }
    });
}

    // --- Public Methods (Действия) ---

    /**
     * Подписывается на общий топик игровой сессии.
     * Вызываем это, когда получили session_created или join_session.
     */
    public subscribeToSession(sessionId: string) {
        console.log(`Subscribing to session topic: /topic/session/${sessionId}/game-updates`);
        
        this.client.subscribe(`/topic/session/${sessionId}/game-updates`, (message: IMessage) => {
            this.processMessage(message);
        });
    }

    /**
     * Отправка запроса на создание сессии
     */
    public createSession(username: string, templateId: string) {
        if (!this.client.active) {
            console.error("Cannot send message: Socket not connected");
            return;
        }

        this.client.publish({
            destination: '/app/create-session',
            body: JSON.stringify({ 
                username, 
                templateId, 
                level: "dungeon_1" // Хардкодим уровень пока что
            })
        });
    }

    // --- Helpers ---

    /**
     * Парсит сообщение и передает его в React (если хендлер установлен)
     */
    private processMessage(message: IMessage) {
        try {
            const event = JSON.parse(message.body);
            console.log('📩 EVENT RECEIVED:', event.eventType, event);

            if (this.eventHandler) {
                this.eventHandler(event);
            }
        } catch (e) {
            console.error("Failed to parse message body", e);
        }
    }

    // Внутри класса GameSocket

    // Добавь этот вызов в onConnect
    // this.subscribeToStateSnapshot();

    private subscribeToStateSnapshot() {
        // Слушаем ответ на запрос GetSessionState
        this.client.subscribe('/user/queue/state', (message) => {
            const wrapper = JSON.parse(message.body);
            // Если бэкенд присылает { eventType: "state_snapshot", payload: ... }
            console.log('📦 SNAPSHOT RECEIVED:', wrapper);
            
            if (this.eventHandler) {
                this.eventHandler(wrapper);
            }
        });
    }

    // Публичный метод для запроса
    public requestSessionState(sessionId: string) {
        console.log("Requesting state for session:", sessionId);
        this.client.publish({
            destination: `/app/session/${sessionId}/state`,
            body: '{}'
        });
    }

    public move(sessionId: string, target: { q: number; r: number }) {
        console.log("Sending Move:", target);
        this.client.publish({
            destination: `/app/session/${sessionId}/move`,
            body: JSON.stringify({ sessionId, target })
        });
    }

    public attack(sessionId: string, attackerId: string, targetId: string) {
        console.log(`Sending Attack: ${attackerId} -> ${targetId}`);
        this.client.publish({
            destination: `/app/session/${sessionId}/attack`,
            body: JSON.stringify({ sessionId, attackerId, targetId })
        });
    }

    public endTurn(sessionId: string) {
        this.client.publish({
            destination: `/app/session/${sessionId}/endTurn`,
            body: JSON.stringify({ sessionId })
        });
        
    }
    // src/api/GameSocket.ts

    public joinSession(sessionId: string, username: string, templateId: string) {
        if (!this.client.active) {
            console.error("Socket not connected");
            return;
        }
        
        console.log(`Joining session ${sessionId} as ${username}`);
        
        this.client.publish({
            destination: '/app/join-session',
            body: JSON.stringify({ 
                sessionId, 
                username, 
                templateId 
            })
        });
    }
    
    public requestPlayerClasses() {
        if (!this.client.active) return;
        console.log("Requesting player classes...");
        this.client.publish({
            destination: '/app/get-classes',
            body: '{}'
        });
    }
    
    public proposePeace(sessionId: string) {
        if (!this.client.active) return;
        console.log("🏳️ Proposing Peace...");
        this.client.publish({
            destination: `/app/session/${sessionId}/combat/propose-peace`,
            body: JSON.stringify({ sessionId })
        });
    }

    public respondToPeace(sessionId: string, accept: boolean) {
        if (!this.client.active) return;
        console.log(`🏳️ Peace Response: ${accept ? "YES" : "NO"}`);
        this.client.publish({
            destination: `/app/session/${sessionId}/combat/respond-peace`,
            body: JSON.stringify({ sessionId, accept })
        });
    }

}

// Экспортируем как синглтон
export const gameSocket = new GameSocket();