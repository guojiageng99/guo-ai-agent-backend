package com.guo.guoaiagentbackend.chatmemory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于 PostgreSQL + JdbcTemplate 的 {@link ChatMemory} 实现。
 */
@Component
@Slf4j
public class PgJdbcChatMemory implements ChatMemory {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PgJdbcChatMemory(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        jdbcTemplate.update(
                """
                        INSERT INTO chat_conversation (conversation_id, updated_at)
                        VALUES (?, now())
                        ON CONFLICT (conversation_id) DO UPDATE SET updated_at = now()
                        """,
                conversationId);

        int n = messages.size();
        Integer endSeq = jdbcTemplate.queryForObject(
                """
                        UPDATE chat_conversation
                        SET next_seq = next_seq + ?, updated_at = now()
                        WHERE conversation_id = ?
                        RETURNING next_seq
                        """,
                Integer.class,
                n,
                conversationId);
        if (endSeq == null) {
            throw new IllegalStateException("conversation missing after upsert: " + conversationId);
        }
        int startSeq = endSeq - n + 1;
        int i = 0;
        for (Message message : messages) {
            int seq = startSeq + (i++);
            Row row = toRow(message);
            jdbcTemplate.update(
                    """
                            INSERT INTO chat_message (conversation_id, seq, message_type, text_content, metadata)
                            VALUES (?, ?, ?, ?, CAST(? AS jsonb))
                            """,
                    conversationId,
                    seq,
                    row.messageType(),
                    row.textContent(),
                    row.metaJsonForDb());
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        return jdbcTemplate.query(
                """
                        SELECT message_type, text_content, metadata::text AS metadata
                        FROM chat_message
                        WHERE conversation_id = ?
                        ORDER BY seq
                        """,
                messageRowMapper(),
                conversationId);
    }

    @Override
    @Transactional
    public void clear(String conversationId) {
        jdbcTemplate.update("DELETE FROM chat_message WHERE conversation_id = ?", conversationId);
        jdbcTemplate.update(
                "UPDATE chat_conversation SET next_seq = 0, updated_at = now() WHERE conversation_id = ?",
                conversationId);
    }

    private RowMapper<Message> messageRowMapper() {
        return (rs, rowNum) -> fromResultSet(rs);
    }

    private Message fromResultSet(ResultSet rs) throws SQLException {
        String type = rs.getString("message_type");
        String text = rs.getString("text_content");
        if (text == null) {
            text = "";
        }
        String metaStr = rs.getString("metadata");
        MessageType mt = parseMessageType(type);
        if (mt == MessageType.TOOL) {
            return toolMessageFromJson(metaStr, text);
        }
        return switch (mt) {
            case USER -> new UserMessage(text);
            case ASSISTANT -> new AssistantMessage(text);
            case SYSTEM -> new SystemMessage(text);
            default -> new UserMessage(text);
        };
    }

    private static MessageType parseMessageType(String type) {
        if (type == null || type.isEmpty()) {
            return MessageType.USER;
        }
        try {
            return MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MessageType.USER;
        }
    }

    private Message toolMessageFromJson(String metaStr, String fallbackText) {
        if (metaStr == null || metaStr.isBlank()) {
            return new ToolResponseMessage(List.of());
        }
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(metaStr, new TypeReference<>() {});
            var responses = new ArrayList<ToolResponseMessage.ToolResponse>();
            for (Map<String, Object> m : raw) {
                String id = stringVal(m.get("id"));
                String name = stringVal(m.get("name"));
                String data = stringVal(m.get("responseData"));
                responses.add(new ToolResponseMessage.ToolResponse(id, name, data));
            }
            return new ToolResponseMessage(responses);
        } catch (JsonProcessingException e) {
            log.warn("解析 TOOL 消息 metadata 失败，退回空工具响应: {}", e.getMessage());
            return new ToolResponseMessage(List.of());
        }
    }

    private static String stringVal(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private Row toRow(Message message) {
        MessageType type = message.getMessageType();
        if (type == MessageType.TOOL && message instanceof ToolResponseMessage tr) {
            try {
                String json = objectMapper.writeValueAsString(toToolPayload(tr));
                return Row.tool(type.name(), json);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("序列化工具消息失败", e);
            }
        }
        String text = message.getText();
        if (text == null) {
            text = "";
        }
        return Row.text(type.name(), text);
    }

    private List<Map<String, String>> toToolPayload(ToolResponseMessage tr) {
        List<Map<String, String>> list = new ArrayList<>();
        for (ToolResponseMessage.ToolResponse r : tr.getResponses()) {
            list.add(Map.of(
                    "id", r.id() != null ? r.id() : "",
                    "name", r.name() != null ? r.name() : "",
                    "responseData", r.responseData() != null ? r.responseData() : ""));
        }
        return list;
    }

    private record Row(String messageType, String textContent, String metaJsonForDb) {
        static Row text(String messageType, String textContent) {
            return new Row(messageType, textContent, "null");
        }

        static Row tool(String messageType, String metadataJson) {
            return new Row(messageType, "", metadataJson);
        }
    }
}
