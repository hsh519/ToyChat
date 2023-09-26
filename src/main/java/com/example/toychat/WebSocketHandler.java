package com.example.toychat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;

    // WebSocketSession - WebSocket 연결을 나타내는 객체
    // 현재 열려 있는 모든 WebSocket 세션들을 저장
    private final Set<WebSocketSession> sessions = new HashSet<>();

    // 채팅 방에 대한 정보를 저장하고 각 채팅 방에 속한 WebSocket 세션들의 집합을 관리. 채팅방 ID: { session1, session2 }
    private final Map<Long, Set<WebSocketSession>> chatRoomSessionMap = new HashMap<>();

    // 소켓 연결 확인
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("{} 연결됨", session.getId());
        sessions.add(session);
    }

    // 소켓 통신 시 메세지의 전송을 다루는 부분
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 받은 메시지의 내용을 가져옴
        String payload = message.getPayload();
        log.info("payload {}", payload);

        // 메시지 내용을 Java 객체로 변환
        ChatMessageDto chatMessageDto = mapper.readValue(payload, ChatMessageDto.class);
        log.info("session {}", chatMessageDto.toString());

        // 채팅 메시지에 포함된 채팅방 ID를 가져옴
        Long chatRoomId = chatMessageDto.getChatRoomId();

        // 해당 채팅방 세션이 맵에 존재하지 않으면, 새 HashSet 을 만들어 세션을 추가
        if (!chatRoomSessionMap.containsKey(chatRoomId)) {
            chatRoomSessionMap.put(chatRoomId, new HashSet<>());
        }

        // 해당 채팅방의 세션들을 가져옴
        Set<WebSocketSession> chatRoomSession = chatRoomSessionMap.get(chatRoomId);

        // 만약 채팅 메시지 타입이 ENTER 라면 현재 세션을 채팅방 세션에 추가
        if (chatMessageDto.getMessageType().equals("ENTER")) {
            chatRoomSession.add(session);
        }

        // 채팅방 세션이 3개 이상이라면 채팅방 세션 정리
        if (chatRoomSession.size() >= 3) {
            removeClosedSession(chatRoomSession);
        }

        sendMessageToChatRoom(chatMessageDto, chatRoomSession);
    }

    // 채팅방에 포함된 세션 중 현재 열려있지 않는 세션은 제거하는 메서드
    private void removeClosedSession(Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.removeIf(sessions::contains);
    }

    // 특정 채팅방의 모든 WebSocket 세션에 메시지를 전송하는 메서드
    private void sendMessageToChatRoom(ChatMessageDto chatMessageDto, Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.parallelStream().forEach(sess -> sendMessage(sess, chatMessageDto));
    }

    // 메시지 전송 메서드
    public <T> void sendMessage(WebSocketSession session, T message) {
        try{
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
