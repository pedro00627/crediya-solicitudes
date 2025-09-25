package co.com.pragma.sqs.sender;

import co.com.pragma.model.events.ApplicationStatusEvent;
import co.com.pragma.model.gateways.NotificationGateway;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificationGateway {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> sendApplicationStatusChange(ApplicationStatusEvent event) {
        return Mono.fromCallable(() -> serializeEvent(event))
                .flatMap(this::send)
                .doOnSuccess(messageId -> log.info("Application status change sent to SQS: {} for application: {}",
                    messageId, event.applicationId()))
                .doOnError(error -> log.error("Failed to send application status change for application: {}",
                    event.applicationId(), error));
    }

    public Mono<String> send(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }

    private String serializeEvent(ApplicationStatusEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ApplicationStatusEvent", e);
        }
    }
}
