package com.example.toychat.chat;

import com.example.toychat.websocket.WebSocketHandler;
import com.example.toychat.chatroom.ChatRoom;
import com.example.toychat.chatroom.ChatRoomRepository;
import com.example.toychat.member.Member;
import com.example.toychat.member.MemberRepository;
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
    @GetMapping("/chat/chatList")
    public String chatList(Model model, HttpServletRequest request) {
        Member loginMember = getMember(request);

        List<List<Object>> chatMemberList = new ArrayList<>();
        for (ChatRoom chatRoom : chatRoomRepository.findAllByMember(loginMember)) {
            if (chatRoom.getSender().getUsername().equals(loginMember.getUsername())) {
                chatMemberList.add(Arrays.asList(chatRoom.getReceiver().getId(), chatRoom.getReceiver().getUsername()));
            } else {
                chatMemberList.add(Arrays.asList(chatRoom.getSender().getId(), chatRoom.getSender().getUsername()));
            }
        }

        model.addAttribute("chatMemberList", chatMemberList);
        return "chatList";
    }

    @Transactional
    @GetMapping("/chat/chatRoom")
    public String createRoomForm(@RequestParam Long receiverId, HttpServletRequest request, Model model) {
        Member sendMember = getMember(request);
        model.addAttribute("sender", sendMember);

        Member receiverMember = memberRepository.findById(receiverId).get();

        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findBySenderAndReceiver(sendMember, receiverMember);

        if (optionalChatRoom.isPresent()) {
            // 메시지를 가져옴
            List<ChatMessageDto> chatMessageList = messageRepository.findAllByChatRoom(optionalChatRoom.get());

            model.addAttribute("room", optionalChatRoom.get());
            model.addAttribute("chatMessageList", chatMessageList);

            return "chatRoom";
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSender(sendMember);
        chatRoom.setReceiver(receiverMember);

        ChatRoom createRoom = chatRoomRepository.save(chatRoom);

        Map<Long, Set<WebSocketSession>> chatRoomSessionMap = webSocketHandler.getChatRoomSessionMap();
        chatRoomSessionMap.put(createRoom.getId(), new HashSet<>());

        model.addAttribute("room", chatRoom);
        model.addAttribute("chatMessageList", new ChatMessageDto());

        return "chatRoom";
    }

    private static Member getMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Member loginMember = (Member) session.getAttribute("loginMember");
        return loginMember;
    }
}
