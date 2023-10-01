package com.example.toychat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public List<ChatRoom> findAllRoom() {
        return chatRoomRepository.findAll();
    }

    @Transactional
    public ChatRoom createRoom(String roomName) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(roomName);

        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    @Transactional
    public ChatRoom findRoomById(String roomId) {
        long id = Long.parseLong(roomId);
        return chatRoomRepository.findById(id).get();
    }
}
