package dev.mygame.dto.websocket.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PeaceProposalResultEvent {
    private boolean wasAccepted; // Более говорящее имя, чем 'agreement'
    private String rejectorName;
}
