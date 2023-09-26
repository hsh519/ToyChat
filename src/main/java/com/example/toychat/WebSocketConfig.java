package com.example.toychat;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler; // 웹소켓 통신을 처리해주는 핸들러

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/ws/chat" - WebSocket 연결이 설정될 엔드포인트. 이 경로로 요청하면 웹소켓 핸드쉐이킹
        // setAllowedOrigins("*") - 모든 CORS 요청 혀용
        // 정리하자면 클라이언트가 "/ws/chat" 경로로 요청하면 webSocketHandler 에서 제공하는 웹소켓 기능을 사용할 수 있다.
        registry.addHandler(webSocketHandler, "/ws/chat").setAllowedOrigins("*");

    }
}
