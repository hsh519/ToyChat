package com.example.toychat;

import com.example.toychat.ChatMessageDto.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
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
@Getter
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;

    // WebSocketSession - WebSocket 연결을 나타내는 객체
    // 현재 열려 있는 모든 WebSocket 세션들을 저장
    private final Set<WebSocketSession> sessions = new HashSet<>();

    // 채팅 방에 대한 정보를 저장하고 각 채팅 방에 속한 WebSocket 세션들의 집합을 관리. 채팅방 ID: { session1, session2 }
    private final Map<Long, Set<WebSocketSession>> chatRoomSessionMap = new HashMap<>();

    private final MessageRepository messageRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("afterConnectionEstablished");
    }

    // 소켓 통신 시 메세지의 전송을 다루는 부분
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("handleTextMessage");
        // 받은 메시지의 내용을 가져옴
        String payload = message.getPayload();

        // 메시지 내용을 Java 객체로 변환
        ChatMessageDto chatMessageDto = mapper.readValue(payload, ChatMessageDto.class);
        System.out.println("chatMessageDto.toString() = " + chatMessageDto.toString());
        messageRepository.save(chatMessageDto);

        // 현재 세션이 세션 Set 에 없으면 추가
        if (session.getAttributes().get("memberId") == null) {
            session.getAttributes().put("memberId", chatMessageDto.getId());
            sessions.add(session);
        }

        // 채팅 메시지에 포함된 채팅방 ID를 가져옴
        Long chatRoomId = chatMessageDto.getChatRoom().getId();

        // 해당 채팅방의 세션들을 가져옴
        Set<WebSocketSession> chatRoomSession = chatRoomSessionMap.get(chatRoomId);

        // 만약 채팅 메시지 타입이 ENTER 라면 현재 세션을 채팅방 세션에 추가
        if (chatMessageDto.getMessageType().equals(MessageType.ENTER)) {
            chatRoomSession.add(session);
        } else if(chatMessageDto.getMessageType().equals(MessageType.QUIT)) {
            chatRoomSession.remove(session);
        }

        // 채팅방 세션이 3개 이상이라면 채팅방 세션 정리
        if (sessions.size() >= 1000) {
            removeClosedSession(sessions, session);
        }

        sendMessageToChatRoom(chatMessageDto, chatRoomSession);
    }

    // 채팅방에 포함된 세션 중 현재 열려있지 않는 세션은 제거하는 메서드
    private void removeClosedSession(Set<WebSocketSession> sessions, WebSocketSession session) {
        sessions.removeIf(s -> !session.equals(s));
    }

    // 특정 채팅방의 모든 WebSocket 세션에 메시지를 전송하는 메서드
    private void sendMessageToChatRoom(ChatMessageDto chatMessageDto, Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.parallelStream().forEach(sess -> sendMessage(sess, chatMessageDto.getMessage()));
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
