package com.example.toychat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 채팅방 목록
    @GetMapping("/chat/chatList")
    public String chatList(Model model) {
        List<ChatRoom> roomList = chatService.findAllRoom();
        model.addAttribute("roomList", roomList);
        return "chatList";
    }

    @GetMapping("/chat/chatRoom")
    public String createRoomForm(@RequestParam String roomId, Model model) {
        ChatRoom findRoom = chatService.findRoomById(roomId);
        model.addAttribute("room", findRoom);
        return "chatRoom";
    }

    // 채팅방 만들기
    @PostMapping("/chat/createRoom")
    public String createRoom(@RequestParam("roomName") String roomName,
                             @RequestParam("username") String username,
                             Model model) {
        ChatRoom room = chatService.createRoom(roomName);
        model.addAttribute("room", room);
        model.addAttribute("username", username);
        return "chatRoom";
    }
}
