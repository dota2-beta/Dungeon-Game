package dev.mygame.domain.event;

import dev.mygame.domain.session.GameSession;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SessionEvent<T> extends ApplicationEvent {
    private final GameSession session;
    private final T payload;

    public SessionEvent(Object source, GameSession session, T payload) {
        super(source);
        this.session = session;
        this.payload = payload;
    }
}
