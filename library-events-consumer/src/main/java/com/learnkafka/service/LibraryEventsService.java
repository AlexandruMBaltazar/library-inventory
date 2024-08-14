package com.learnkafka.service;

import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.exception.RetryableMessagingException;
import com.learnkafka.jpa.LibraryEventsRepository;
import com.learnkafka.mapper.LibraryEventMapper;
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
        log.info("libraryEvent: {}", libraryEvent);

        switch (libraryEvent.getLibraryEventType()) {
            case NEW -> save(libraryEvent);
            case UPDATE -> update(libraryEvent);
            default -> log.error("Invalid Library Event Type");
        }
    }

    private void update(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventId() == null) {
            log.warn("Library Event ID is missing");
            throw new RetryableMessagingException("Library Event ID is missing");
        }

        if (!libraryEventsRepository.existsByLibraryEventId(libraryEvent.getLibraryEventId())) {
            log.warn("Library Event not found with Id: {} - retrying update event", libraryEvent.getLibraryEventId());
            throw new RetryableMessagingException("Library Event found with Id: " +
                    libraryEvent.getLibraryEventId() + "- retrying update event");
        }

        save(libraryEvent);
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("Successfully saved new library event");
    }
}
