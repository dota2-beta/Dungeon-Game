package dev.mygame.dto.websocket.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PeaceProposalEvent {
    private String initiatorId;
    private String initiatorName;
}
