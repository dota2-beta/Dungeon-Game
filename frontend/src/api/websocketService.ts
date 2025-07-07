import { Client, type IFrame, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export let stompClient: Client | null = null;

export const connect = (
    onStompConnectCallback: (frame: IFrame) => void,
    onErrorCallback: (error: any) => void
) => {

    if (stompClient?.active) return;

    stompClient = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/gs-websocket'),
        
        debug: (str) => console.log('STOMP Debug:', str),
        reconnectDelay: 5000,
    });

    stompClient.onConnect = onStompConnectCallback;
    stompClient.onStompError = onErrorCallback;
    stompClient.onWebSocketError = onErrorCallback;

    stompClient.activate();
};

/**
 * Отключает и деактивирует STOMP-клиент.
 */
export const disconnect = () => {
    if (stompClient?.active) {
        stompClient.deactivate();
        console.log('WebSocket client deactivated.');
    }
    stompClient = null;
};

/**
 * Подписывается на указанный STOMP-топик.
 * @param topic - Адрес топика (например, '/topic/session/123/updates').
 * @param callback - Функция, которая будет вызвана при получении сообщения.
 * @returns Объект подписки, который можно использовать для отписки.
 */
export const subscribe = <T>(topic: string, callback: (payload: T) => void) => {
    if (stompClient && stompClient.connected) {
        return stompClient.subscribe(topic, (message: IMessage) => {
            callback(JSON.parse(message.body) as T);
        });
    } else {
        console.error('Cannot subscribe, STOMP client is not connected.');
        return null;
    }
};

/**
 * Отправляет (публикует) сообщение на указанный STOMP-адрес.
 * @param destination - Адрес назначения (например, '/app/create-session').
 * @param body - Объект, который будет преобразован в JSON и отправлен в теле сообщения.
 */
export const publish = (destination: string, body: object) => {
    if (stompClient && stompClient.connected) {
        stompClient.publish({
            destination: destination,
            body: JSON.stringify(body),
        });
    } else {
        console.error('Cannot publish, STOMP client is not connected.');
    }
};

/**
 * Возвращает текущее состояние клиента (подключен ли он).
 */
export const isConnected = (): boolean => {
    return stompClient?.connected ?? false;
};