package com.abs.dungeoncrawler.gamesessionservice.exception;

import lombok.Getter;

@Getter
public class GameActionException extends RuntimeException {
    private final String errorCode;

    public GameActionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
