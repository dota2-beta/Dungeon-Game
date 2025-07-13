package dev.mygame.mapper.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MappingContext {
    private final String forUserId;
    private final int mapRadius;
}