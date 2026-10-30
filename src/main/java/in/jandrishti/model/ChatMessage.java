package in.jandrishti.model;

import java.time.LocalDateTime;

public record ChatMessage(
        String id,
        String sender,
        String text,
        boolean isAi,
        LocalDateTime timestamp,
        Long relatedIssueId,
        String actionLabel
) {
    public static ChatMessage user(String text) {
        return new ChatMessage(
                "msg-" + System.currentTimeMillis(),
                "Officer / Citizen",
                text,
                false,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static ChatMessage ai(String text) {
        return new ChatMessage(
                "msg-" + System.currentTimeMillis(),
                "JanDrishti Copilot AI",
                text,
                true,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static ChatMessage aiWithAction(String text, long issueId, String actionLabel) {
        return new ChatMessage(
                "msg-" + System.currentTimeMillis(),
                "JanDrishti Copilot AI",
                text,
                true,
                LocalDateTime.now(),
                issueId,
                actionLabel
        );
    }
}
