package com.example.demo.component;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class websocketComponent extends TextWebSocketHandler {
    private static final AtomicInteger activeConnections = new AtomicInteger(0);
    private static final List<WebSocketSession> sessions = new ArrayList<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        activeConnections.incrementAndGet();
        sendMessageToAll("Nouvelle connexion établie. Nombre total de connexions : " + activeConnections.get());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        activeConnections.decrementAndGet();
        sendMessageToAll("Connexion fermée. Nombre total de connexions : " + activeConnections.get());
    }

    private void sendMessageToAll(String message) {
        for (WebSocketSession session : this.sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                // Gérer l'erreur de l'envoi du message
            }
        }
    }
}
