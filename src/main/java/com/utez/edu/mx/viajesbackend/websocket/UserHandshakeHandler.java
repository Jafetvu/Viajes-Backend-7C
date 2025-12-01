package com.utez.edu.mx.viajesbackend.websocket;

import com.utez.edu.mx.viajesbackend.security.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
    private final Logger logger = LoggerFactory.getLogger(UserHandshakeHandler.class);
    private final JWTUtil jwtUtil;

    public UserHandshakeHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            URI uri = request.getURI();
            String query = uri.getQuery();
            
            if (query != null && query.contains("token=")) {
                String token = extractTokenFromQuery(query);
                if (token != null) {
                    String username = jwtUtil.extractUsername(token);
                    if (username != null) {
                        logger.info("UserHandshakeHandler determined user: {}", username);
                        return new StompPrincipal(username);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error determining user from handshake: {}", e.getMessage());
        }
        return null;
    }

    private String extractTokenFromQuery(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }
}
