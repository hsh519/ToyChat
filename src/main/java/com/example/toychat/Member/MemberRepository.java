package com.example.toychat.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByUsername(String username);

    @Query("select m from Member m where not m.id = :id")
    List<Member> findMemberList(@Param("id") Long id);
}
