package com.timcritt.tfg.infrastructure.messaging;
import com.timcritt.tfg.application.port.outbound.RoleEventPublisherPort;
import com.timcritt.tfg.domain.event.TeacherRoleRevokedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component
public class KafkaRoleEventPublisherAdapter implements RoleEventPublisherPort {
    private static final Logger log = LoggerFactory.getLogger(KafkaRoleEventPublisherAdapter.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String teacherRoleRevokedTopic;
    public KafkaRoleEventPublisherAdapter(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.teacher-role-revoked:user.teacher-role-revoked}") String teacherRoleRevokedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.teacherRoleRevokedTopic = teacherRoleRevokedTopic;
    }
    @Override
    public void publishTeacherRoleRevoked(TeacherRoleRevokedEvent event) {
        log.info("Publishing TeacherRoleRevokedEvent for userId={}", event.userId());
        kafkaTemplate.send(teacherRoleRevokedTopic, String.valueOf(event.userId()), event);
    }
}
