package com.abs.dungeoncrawler.gamesessionservice.kafka;

import com.abs.dungeonCrawler.eventcontracts.EntityJoinedEvent;
import com.abs.dungeonCrawler.eventcontracts.PlayerJoinedEvent;
import com.abs.dungeonCrawler.eventcontracts.SessionCreatedEvent;
import com.abs.dungeonCrawler.eventcontracts.eventDto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPlayerJoinedEvent(PlayerJoinedEvent event) {
        kafkaTemplate.send("session-management-events", event.getSessionId(), event);
    }
    public void publishEntityJoinedEvent(EntityJoinedEvent event) {
        kafkaTemplate.send("session-management-events", event.getSessionId(), event);
    }
    public void publishSessionCreatedEvent(SessionCreatedEvent event) {
        kafkaTemplate.send("session-management-events", event.getSessionId(), event);
    }
    public void publishEntityMovedEvent(EntityMovedEvent event) {
        kafkaTemplate.send("entity-state-events", event.getSessionId(), event);
    }
    public void publishEntityAttackEvent(EntityAttackEvent event) {
        kafkaTemplate.send("entity-state-events", event.getSessionId(),  event);
    }
    public void publishEntityStatsUpdatedEvent(EntityStatsUpdatedEvent event) {
        kafkaTemplate.send("entity-state-events", event.getSessionId(), event);
    }
    public void publishCombatEndedEvent(CombatEndedEvent event) {
        kafkaTemplate.send("combat-events", event.getSessionId(), event);
    }
    public void publishCombatStartedEvent(CombatStartedEvent event) {
        kafkaTemplate.send("combat-events", event.getSessionId(), event);
    }
    public void publishCombatParticipantsJoinedEvent(CombatParticipantsJoinedEvent event) {
        kafkaTemplate.send("combat-events", event.getSessionId(), event);
    }
    public void publishCombatTurnChangedEvent(CombatTurnChangedEvent event) {
        kafkaTemplate.send("combat-events", event.getSessionId(), event);
    }
    public void publishEntityDiedEvent(EntityDiedEvent deathEvent) {
        kafkaTemplate.send("entity-state-events", deathEvent.getSessionId(), deathEvent);
    }
    public void publishPeaceProposalEvent(PeaceProposalEvent event) {
        kafkaTemplate.send("combat-events", event.getSessionId(), event);
    }
    public void publishPeaceResultEvent(PeaceResultEvent event) {
        kafkaTemplate.send("combat-events", event.getSessionId(), event);
    }
}
