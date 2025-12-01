package com.niyiment.patientservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niyiment.patientservice.event.PatientProgramEnrolledEvent;
import com.niyiment.patientservice.event.PatientRegisteredEvent;
import com.niyiment.patientservice.event.PatientUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for publishing patient events to Kafka.
 * Listens to internal Spring events and publishes them to Kafka topics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientEventPublisher {

    private static final String PATIENT_REGISTERED_TOPIC = "patient.registered";
    private static final String PATIENT_UPDATED_TOPIC = "patient.updated";
    private static final String PATIENT_PROGRAM_ENROLLED_TOPIC = "patient.program.enrolled";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publishes PatientRegisteredEvent to Kafka.
     */
    @Async
    @EventListener
    public void handlePatientRegistered(PatientRegisteredEvent event) {
        publishEvent(PATIENT_REGISTERED_TOPIC, event.patientId().toString(), event);
    }

    /**
     * Publishes PatientUpdatedEvent to Kafka.
     */
    @Async
    @EventListener
    public void handlePatientUpdated(PatientUpdatedEvent event) {
        publishEvent(PATIENT_UPDATED_TOPIC, event.patientId().toString(), event);
    }

    /**
     * Publishes PatientProgramEnrolledEvent to Kafka.
     */
    @Async
    @EventListener
    public void handlePatientProgramEnrolled(PatientProgramEnrolledEvent event) {
        publishEvent(PATIENT_PROGRAM_ENROLLED_TOPIC, event.patientId().toString(), event);
    }

    private void publishEvent(String topic, String key, Object event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event to topic {}: {}", topic, ex.getMessage(), ex);
                } else {
                    log.info("Successfully published event to topic {}, partition {}, offset {}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                    );
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event for topic {}: {}", topic, e.getMessage(), e);
        }
    }
}