package com.learnkafka.service;

import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.exception.RetryableMessagingException;
import com.learnkafka.jpa.LibraryEventsRepository;
import com.learnkafka.mapper.LibraryEventMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsService {

    private final LibraryEventMapper libraryEventMapper;
    private final LibraryEventsRepository libraryEventsRepository;

    public void processLibraryEvent(ConsumerRecord<Long, com.learnkafka.model.LibraryEvent> consumerRecord) {
        LibraryEvent libraryEvent = libraryEventMapper.libraryEventToLibraryEventEntity(consumerRecord.value());
        log.info("Processing library event: {}", libraryEvent);

        try {
            switch (libraryEvent.getLibraryEventType()) {
                case NEW -> save(libraryEvent);
                case UPDATE -> update(libraryEvent);
                default -> log.error("Invalid Library Event Type: {}", libraryEvent.getLibraryEventType());
            }
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            log.error("Error processing library event: {} - Exception: {}", libraryEvent, e.getMessage(), e);
            throw new RetryableMessagingException("Error processing library event");
        } catch (Exception e) {
            log.error("Unexpected error processing library event: {} - Exception: {}", libraryEvent, e.getMessage(), e);
            throw new RetryableMessagingException("Unexpected error", e);
        }
    }

    private void update(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventId() == null) {
            throw new IllegalArgumentException("Library Event ID is missing");
        }

        if (!libraryEventsRepository.existsByLibraryEventId(libraryEvent.getLibraryEventId())) {
            throw new EntityNotFoundException("Library Event not found with Id: " + libraryEvent.getLibraryEventId());
        }

        save(libraryEvent);
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        try {
            libraryEventsRepository.save(libraryEvent);
            log.info("Successfully saved new library event");
        } catch (Exception e) {
            log.error("Error saving library event: {} - Exception: {}", libraryEvent, e.getMessage(), e);
            throw e; // Ensure exceptions during save are retried
        }
    }
}
