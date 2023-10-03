package com.example.toychat.chat;

import com.example.toychat.chatroom.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<ChatMessageDto, Long> {
    List<ChatMessageDto> findAllByChatRoom(ChatRoom chatRoom);
}
