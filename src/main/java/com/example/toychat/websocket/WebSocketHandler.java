package com.example.toychat.websocket;

import com.example.toychat.chat.ChatMessageDto;
import com.example.toychat.chat.ChatMessageDto.MessageType;
import com.example.toychat.chat.MessageRepository;
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

    // 소켓 통신 시 메세지의 전송을 다루는 부분
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 받은 메시지의 내용을 가져옴
        String payload = message.getPayload();

        // 메시지 내용을 Java 객체로 변환
        ChatMessageDto chatMessage = mapper.readValue(payload, ChatMessageDto.class);

        // 현재 세션이 세션 Set 에 없으면 추가
        if (session.getAttributes().get("memberId") == null) {
            session.getAttributes().put("memberId", chatMessage.getSender().getId());
            sessions.add(session);
        }

        // 채팅 메시지에 포함된 채팅방 ID를 가져옴
        Long chatRoomId = chatMessage.getChatRoom().getId();

        // 해당 채팅방의 세션들을 가져옴
        Set<WebSocketSession> chatRoomSession = chatRoomSessionMap.get(chatRoomId);
        System.out.println("chatRoomSession = " + chatRoomSession);

        // 만약 채팅 메시지 타입이 TALK 이고 첫 채팅이라면 해당 채팅방에 세션 추가
        //  방을 들어갔다 나간 뒤 채팅을 치면 세션 id가 달라지는데 그걸 memberId로 제어를 해서 새로 생긴 세션이 추가가 채팅방 세션 Map 에 추가가 안되는 오류가 생김 (해결 필요)
        if (chatMessage.getMessageType().equals(MessageType.TALK)) {
            if (chatRoomSession.stream().filter(s -> s.getAttributes().get("memberId") == chatMessage.getSender().getId()).count() == 1) {
                chatRoomSession.add(session);
            }
            messageRepository.save(chatMessage);
        } else if(chatMessage.getMessageType().equals(MessageType.QUIT)) {
            chatRoomSession.remove(session);
        }

        // 누적 세션이 1000개 이상이라면 채팅방 세션 정리
        if (sessions.size() >= 1000) {
            removeClosedSession(sessions, session);
        }

        sendMessageToChatRoom(chatMessage, chatRoomSession);
    }

    // 채팅방에 포함된 세션 중 현재 열려있지 않는 세션은 제거하는 메서드
    private void removeClosedSession(Set<WebSocketSession> sessions, WebSocketSession session) {
        sessions.removeIf(s -> !session.equals(s));
    }

    // 특정 채팅방의 모든 WebSocket 세션에 메시지를 전송하는 메서드
    private void sendMessageToChatRoom(ChatMessageDto chatMessage, Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.parallelStream().forEach(sess -> sendMessage(sess, chatMessage.getMessage()));
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
