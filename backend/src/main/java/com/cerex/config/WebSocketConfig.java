package com.cerex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time features.
 *
 * <p>Supports:
 * <ul>
 *   <li>Order tracking (status updates, GPS location)</li>
 *   <li>Live notifications (likes, comments, follows)</li>
 *   <li>Kitchen display system (new orders for restaurants)</li>
 * </ul>
 *
 * <p>Uses STOMP protocol over WebSocket with SockJS fallback.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory broker for subscriptions
        // Topics: /topic/orders/{orderId}, /topic/notifications/{userId}, /topic/kitchen/{restaurantId}
        config.enableSimpleBroker("/topic", "/queue");

        // Application destination prefix
        config.setApplicationDestinationPrefixes("/app");

        // User-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Primary WebSocket endpoint
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();

        // Native WebSocket endpoint (for mobile apps)
        registry.addEndpoint("/ws-native")
            .setAllowedOriginPatterns("*");
    }
}
