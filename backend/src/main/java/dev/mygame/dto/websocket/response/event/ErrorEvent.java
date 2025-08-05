package dev.mygame.dto.websocket.response.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorEvent {
    private String message;
    private String errorCode;

    public ErrorEvent(String message) {
        this.message = message;
        this.errorCode = null;
    }
}
