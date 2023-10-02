package com.example.toychat;

import com.example.toychat.Member.Member;
import com.example.toychat.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final WebSocketHandler webSocketHandler;

    // 채팅방 목록
//    @GetMapping("/chat/chatList")
//    public String chatList(Model model, HttpServletRequest request) {
//        HttpSession session = request.getSession(false);
//        Member loginMember = (Member) session.getAttribute("loginMember");
//
//        List<ChatRoom> roomList = chatService.findMyChatRoom(loginMember);
//        model.addAttribute("roomList", roomList);
//        return "chatList";
//    }

    @Transactional
    @GetMapping("/chat/chatRoom")
    public String createRoomForm(@RequestParam Long receiverId, HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        Member sendMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("sender", sendMember);

        Member receiverMember = memberRepository.findById(receiverId).get();

        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findBySenderAndReceiver(sendMember, receiverMember);

        if (optionalChatRoom.isPresent()) {
            // 메시지를 가져옴
            ChatRoom chatRoom = optionalChatRoom.get();
            List<ChatMessageDto> chatMessageList = messageRepository.findAllByChatRoom(chatRoom);
            model.addAttribute("room", chatRoom);
            model.addAttribute("chatMessageList", chatMessageList);
            System.out.println(webSocketHandler.getChatRoomSessionMap().get(chatRoom.getId()));
            return "chatRoom";
        }
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSender(sendMember);
        chatRoom.setReceiver(receiverMember);
        ChatRoom createRoom = chatRoomRepository.save(chatRoom);

        model.addAttribute("room", chatRoom);

        Map<Long, Set<WebSocketSession>> chatRoomSessionMap = webSocketHandler.getChatRoomSessionMap();
        chatRoomSessionMap.put(createRoom.getId(), new HashSet<>());
        System.out.println(webSocketHandler.getChatRoomSessionMap().get(chatRoom.getId()));
        return "chatRoom";
    }
//
//    // 채팅방 만들기
//    @PostMapping("/chat/createRoom")
//    public String createRoom(@RequestParam("roomName") String roomName, HttpServletRequest request, Model model) {
//        HttpSession session = request.getSession(false);
//        Member member = (Member) session.getAttribute("loginMember");
//
//        ChatRoom room = chatService.createRoom(roomName);
//        model.addAttribute("room", room);
//        model.addAttribute("username", member.getUsername());
//        return "chatRoom";
//    }
}
