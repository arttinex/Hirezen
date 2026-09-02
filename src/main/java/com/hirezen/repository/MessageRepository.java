package com.hirezen.repository;

import com.hirezen.model.Message;
import com.hirezen.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Every message this user sent or received, newest first - grouped into
     * conversations in MessageService. JOIN FETCH pulls sender/recipient in
     * the same query instead of leaving them as lazy proxies - without this,
     * accessing c.partner.name in messages-inbox.html throws
     * LazyInitializationException once the request's session has closed.
     */
    @Query("select m from Message m join fetch m.sender join fetch m.recipient " +
            "where m.sender = :user or m.recipient = :user order by m.sentAt desc")
    List<Message> findAllForUser(@Param("user") User user);

    /** Full back-and-forth history between two specific users, oldest first (chat reading order). */
    @Query("select m from Message m join fetch m.sender join fetch m.recipient " +
            "where (m.sender = :a and m.recipient = :b) or (m.sender = :b and m.recipient = :a) order by m.sentAt asc")
    List<Message> findConversation(@Param("a") User a, @Param("b") User b);
}