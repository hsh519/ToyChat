package com.example.toychat.chatroom;

import com.example.toychat.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor
public class ChatRoom {

    @Id @GeneratedValue
    @Column(name = "ROOM_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "SENDER_ID")
    private Member sender;

    @ManyToOne
    @JoinColumn(name = "RECEIVER_ID")
    private Member receiver;
}
