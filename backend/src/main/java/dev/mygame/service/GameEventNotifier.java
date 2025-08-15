package dev.mygame.service;

import dev.mygame.config.WebSocketDestinations;
import dev.mygame.domain.event.SessionEvent;
import dev.mygame.domain.model.Player;
import dev.mygame.domain.session.GameSession;
import dev.mygame.dto.websocket.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameEventNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionEvent(SessionEvent<?> event) {
        GameSession session = event.getSession();
        Object payload = event.getPayload();

        String eventType = resolveEventType(payload);

        if (eventType != null) {
            String destination = WebSocketDestinations.GAME_UPDATES_TOPIC.replace("{sessionId}", session.getSessionID());
            sendWrappedMessage(destination, eventType, payload);
        }
    }

    // Метод для определения eventType
    private String resolveEventType(Object payload) {
        if (payload instanceof PlayerLeftEvent) return "player_left";
        if (payload instanceof TeamUpdatedEvent) return "team_updated";
        if (payload instanceof EntityAttackEvent) return "entity_attack";
        if (payload instanceof EntityStatsUpdatedEvent) return "entity_stats_updated";
        if (payload instanceof EntityMovedEvent) return "entity_moved";
        if (payload instanceof CombatStartedEvent) return "combat_started";
        if (payload instanceof CombatEndedEvent) return "combat_ended";
        if (payload instanceof CombatNextTurnEvent) return "combat_next_turn";
        //TODO: желательно заменить "entity_turn_ended" на "combat_end_turn"
        if (payload instanceof EntityTurnEndEvent) return "entity_turn_ended";
        if (payload instanceof CombatParticipantsJoinedEvent) return "combat_participants_joined";
        if (payload instanceof EntityDiedEvent) return "entity_died";
        if (payload instanceof CasterStateUpdatedEvent) return "caster_state_updated";
        if (payload instanceof AbilityCastedEvent) return "ability_casted";
        if (payload instanceof PlayerJoinedEvent) return "player_joined";
        // ... и так далее для всех публичных событий
        return null; // или бросить исключение, если тип неизвестен
    }


    /**
     * Отправляет публичное событие всем подписчикам игровой сессии.
     * @param session   Текущая игровая сессия.
     * @param eventType Тип события (например, "player_joined").
     * @param payload   Объект с данными события.
     */
    public void notifySession(GameSession session, String eventType, Object payload) {
        String destination = WebSocketDestinations.GAME_UPDATES_TOPIC.replace("{sessionId}", session.getSessionID());
        sendWrappedMessage(destination, eventType, payload);
    }

    /**
     * Отправляет приватное событие конкретному игроку.
     * @param player    Игрок-получатель.
     * @param eventType Тип события (например, "team_invite").
     * @param payload   Объект с данными события.
     */
    public void notifyPlayer(Player player, String eventType, Object payload) {
        messagingTemplate.convertAndSendToUser(
                player.getUserId(),
                WebSocketDestinations.PRIVATE_EVENTS_QUEUE,
                wrapPayload(eventType, payload)
        );
    }

    /**
     * Отправляет приватное событие группе игроков.
     * @param players   Список игроков-получателей.
     * @param eventType Тип события (например, "peace_proposal_result").
     * @param payload   Объект с данными события.
     */
    public void notifyPlayers(List<Player> players, String eventType, Object payload) {
        Map<String, Object> wrappedMessage = wrapPayload(eventType, payload);
        for (Player player : players) {
            messagingTemplate.convertAndSendToUser(
                    player.getUserId(),
                    WebSocketDestinations.PRIVATE_EVENTS_QUEUE,
                    wrappedMessage
            );
        }
    }

    /**
     * Отправляет сообщение об ошибке конкретному игроку.
     * @param player    Игрок-получатель.
     * @param message   Текст ошибки для пользователя.
     * @param errorCode Код ошибки для клиента.
     */
    public void notifyError(Player player, String message, String errorCode) {
        ErrorEvent errorEvent = new ErrorEvent(message, errorCode);
        messagingTemplate.convertAndSendToUser(
                player.getUserId(),
                WebSocketDestinations.ERROR_QUEUE,
                errorEvent
        );
    }

    /**
     * Отправляет полное состояние игровой сессии конкретному пользователю.
     * Используется при первом подключении или при ресинхронизации.
     * @param userId        ID пользователя-получателя.
     * @param sessionId     ID текущей сессии.
     * @param sessionState  Объект DTO с полным состоянием игры.
     */
    public void notifyFullGameState(String userId, String sessionId, Object sessionState) {
        String destination = WebSocketDestinations.SESSION_STATE_QUEUE
                .replace("{sessionId}", sessionId);

        messagingTemplate.convertAndSendToUser(userId, destination, sessionState);
    }

    private void sendWrappedMessage(String destination, String eventType, Object payload) {
        messagingTemplate.convertAndSend(destination, wrapPayload(eventType, payload));
    }

    private Map<String, Object> wrapPayload(String eventType, Object payload) {
        Map<String, Object> action = new HashMap<>();
        action.put("actionType", eventType);
        action.put("payload", payload);
        return action;
    }
}
