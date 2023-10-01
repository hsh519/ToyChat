package com.example.toychat;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@ToString
public class ChatMessageDto {

    public enum MessageType {
        ENTER, TALK, QUIT
    }

    @Id @GeneratedValue
    private Long id;
    private MessageType messageType; // 메시지 타입
    private String sender; // 보낸 사람
    private String message; // 메시지 내용
    private Long chatRoomId; // 방 ID
}
