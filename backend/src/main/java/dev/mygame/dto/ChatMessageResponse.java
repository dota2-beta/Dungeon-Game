package dev.mygame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ChatMessageResponse {
    private String sender;
    private String message;
    private Date date;
}
