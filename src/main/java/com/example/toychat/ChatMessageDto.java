package com.example.toychat;

import com.example.toychat.Member.Member;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@ToString
public class ChatMessageDto {

    public enum MessageType {
        ENTER, TALK, QUIT
    }

    @Id @GeneratedValue
    @Column(name = "MESSAGE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ROOM_ID")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "SENDER_ID")
    private Member sender; // 보낸 사람

    private MessageType messageType; // 메시지 타입
    private String message; // 메시지 내용
}
