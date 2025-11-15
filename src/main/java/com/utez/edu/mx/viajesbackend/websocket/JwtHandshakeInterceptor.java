package com.utez.edu.mx.viajesbackend.websocket;

import com.utez.edu.mx.viajesbackend.security.CustomUserDetailsService;
import com.utez.edu.mx.viajesbackend.security.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket handshake interceptor that validates JWT tokens.
 *
 * <p>This interceptor extracts the JWT token from the query parameter during
 * the WebSocket handshake and validates it. If valid, the username is stored
 * in the WebSocket session attributes for later use.</p>
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtHandshakeInterceptor(JWTUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        try {
            URI uri = request.getURI();
            String query = uri.getQuery();

            if (query == null || !query.contains("token=")) {
                logger.warn("WebSocket connection attempt without token");
                return false;
            }

            // Extract token from query parameter
            String token = extractTokenFromQuery(query);
            if (token == null || token.isEmpty()) {
                logger.warn("Empty token in WebSocket connection");
                return false;
            }

            // Validate token
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails)) {
                // Store username in session attributes
                attributes.put("username", username);
                logger.info("WebSocket handshake successful for user: {}", username);
                return true;
            } else {
                logger.warn("Invalid JWT token for WebSocket connection");
                return false;
            }

        } catch (Exception e) {
            logger.error("Error during WebSocket handshake: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // No action needed after handshake
    }

    /**
     * Extracts the token value from the query string.
     *
     * @param query the query string containing token parameter
     * @return the token value or null if not found
     */
    private String extractTokenFromQuery(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6); // "token=".length() = 6
            }
        }
        return null;
    }
}
