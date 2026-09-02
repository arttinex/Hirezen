package com.hirezen.model;

/**
 * One row in the message inbox: who the conversation is with, their most
 * recent message, and whether it belongs in Primary (they've replied at
 * least once) or General (a one-sided message, like an unaccepted request).
 */
public record ConversationSummary(User partner, Message lastMessage, boolean primary) {
}
