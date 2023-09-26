package com.example.toychat;

import lombok.Data;

@Data
public class ChatMessageDto {

    private Long chatRoomId;
    private String messageType;
}
