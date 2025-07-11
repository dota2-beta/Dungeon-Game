package dev.mygame.domain.event;

import dev.mygame.enums.ActionType;

public interface IAction {
    ActionType getActionType();
}
