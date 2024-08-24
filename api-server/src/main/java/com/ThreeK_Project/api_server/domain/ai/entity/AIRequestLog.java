package com.ThreeK_Project.api_server.domain.ai.entity;

import com.ThreeK_Project.api_server.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "p_ai_request_logs")
public class AIRequestLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID aiRequestLogId;
    private String question;
    private String answer;

    public static AIRequestLog createAIRequestLog(
            String question, String answer
    ) {
        return AIRequestLog.builder()
                .question(question)
                .answer(answer)
                .build();
    }
}
