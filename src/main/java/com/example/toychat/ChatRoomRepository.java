package com.example.toychat;

import com.example.toychat.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("select m from ChatRoom m where (m.sender = :sender and m.receiver = :receiver) or (m.sender = :receiver and m.receiver = :sender)")
    Optional<ChatRoom> findBySenderAndReceiver(@Param("sender") Member sender, @Param("receiver") Member receiver);
}
