package com.abs.dungeoncrawler.websocketservice.listeners;

import com.abs.dungeonCrawler.eventcontracts.EntityJoinedEvent;
import com.abs.dungeonCrawler.eventcontracts.PlayerJoinedEvent;
import com.abs.dungeonCrawler.eventcontracts.SessionCreatedEvent;
import com.abs.dungeonCrawler.eventcontracts.eventDto.*;
import com.abs.dungeoncrawler.websocketservice.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(
        topics = {"session-management-events", "entity-state-events", "combat-events"},
        groupId = "websocket-consumers")
public class KafkaEventListener {
    private final NotificationService notificationService;

    @KafkaHandler
    public void handlePlayerJoinedEvent (EntityJoinedEvent e) {
        notificationService.notifySessionWithEvent(
                e.getSessionId(),
                "entity_joined",
                e
        );
    }
    @KafkaHandler
    public void handleSessionCreatedEvent(SessionCreatedEvent e) {
        log.info("Session created: {}", e.getSessionId());
        notificationService.notifyUserWithEvent(
                e.getUserId(),
                "session_created",
                e
        );
    }

    // Combat Events

    @KafkaHandler
    public void handleCombatStarted(CombatStartedEvent e) {
        notificationService.notifySessionWithEvent(e.getSessionId(), "combat_started", e);
    }

    @KafkaHandler
    public void handleCombatEnded(CombatEndedEvent e) {
        notificationService.notifySessionWithEvent(e.getSessionId(), "combat_ended", e);
    }

    @KafkaHandler
    public void handleCombatTurnChanged(CombatTurnChangedEvent e) {
        notificationService.notifySessionWithEvent(e.getSessionId(), "combat_turn_changed", e);
    }

    @KafkaHandler
    public void handleCombatParticipantsJoined(CombatParticipantsJoinedEvent e) {
        notificationService.notifySessionWithEvent(e.getSessionId(), "combat_participants_joined", e);
    }

    @KafkaHandler
    public void handlePeaceProposal(PeaceProposalEvent e) {
        log.info("Peace proposed in session {}", e.getSessionId());
        notificationService.notifySessionWithEvent(
                e.getSessionId(),
                "peace_proposal",
                e
        );
    }

    @KafkaHandler
    public void handlePeaceResult(PeaceResultEvent e) {
        log.info("Peace result in session {}: {}", e.getSessionId(), e.isSuccess());
        notificationService.notifySessionWithEvent(
                e.getSessionId(),
                "peace_result",
                e
        );
    }

    //Entity State Events

    @KafkaHandler
    public void handleEntityMovedEvent(EntityMovedEvent e) {
        log.info("Entity moved: {}", e.getEntityId());
        notificationService.notifySessionWithEvent(
                e.getSessionId(),
                "entity_moved",
                e
        );
    }

    @KafkaHandler
    public void handleEntityStatsUpdated(EntityStatsUpdatedEvent e) {
        notificationService.notifySessionWithEvent(e.getSessionId(), "entity_stats_updated", e);
    }

    @KafkaHandler
    public void handleEntityAttack(EntityAttackEvent e) {
        notificationService.notifySessionWithEvent(e.getSessionId(), "entity_attack", e);
    }

    @KafkaHandler
    public void handleEntityDied(EntityDiedEvent e) {
        notificationService.notifySessionWithEvent(e.getSessionId(), "entity_died", e);
    }
}
