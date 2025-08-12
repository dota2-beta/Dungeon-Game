package dev.mygame.dto.websocket.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorEvent {
    private String message;
    private String errorCode;

}
