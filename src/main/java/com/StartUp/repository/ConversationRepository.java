package com.StartUp.repository;

import com.StartUp.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUser1IdOrUser2IdOrderByLastMessageAtDesc(Long user1Id, Long user2Id);

    Optional<Conversation> findByUser1_IdAndUser2_IdOrUser2_IdAndUser1_Id(Long user1Id, Long user2Id, Long user1Id1, Long user2Id1);

    @Query("""
    SELECT c FROM Conversation c
    WHERE (c.user1.id = :u1 AND c.user2.id = :u2)
       OR (c.user1.id = :u2 AND c.user2.id = :u1)
    """)
    Optional<Conversation> findConversationBetween(
            @Param("u1") Long user1Id,
            @Param("u2") Long user2Id);
}