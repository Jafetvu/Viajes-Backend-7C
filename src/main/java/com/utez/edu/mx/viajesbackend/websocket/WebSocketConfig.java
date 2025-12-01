package com.utez.edu.mx.viajesbackend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration class for STOMP messaging.
 *
 * <p>This configuration enables WebSocket support with STOMP protocol,
 * configures message broker, and sets up endpoints for client connections.
 * Supports both WS and WSS protocols.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final com.utez.edu.mx.viajesbackend.security.JWTUtil jwtUtil;

    public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor, com.utez.edu.mx.viajesbackend.security.JWTUtil jwtUtil) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Configure the message broker.
     *
     * <p>Sets up:
     * - Simple broker for broadcasting messages to subscribed clients
     * - Application destination prefix for messages from clients</p>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker
        // Messages with destination starting with /topic or /queue will be routed to the broker
        config.enableSimpleBroker("/topic", "/queue");

        // Messages with destination starting with /app will be routed to @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");

        // Optional: Set user destination prefix for sending messages to specific users
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints for WebSocket connections.
     *
     * <p>Configures:
     * - /ws endpoint for WebSocket connections
     * - JWT authentication via handshake interceptor
     * - SockJS fallback for browsers that don't support WebSocket
     * - CORS configuration for frontend access</p>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new UserHandshakeHandler(jwtUtil))
                .setAllowedOriginPatterns("*") // For development; restrict in production
                .withSockJS(); // Fallback for browsers without WebSocket support

        // Native WebSocket endpoint without SockJS (for WSS support)
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new UserHandshakeHandler(jwtUtil))
                .setAllowedOriginPatterns("*");
    }
}
