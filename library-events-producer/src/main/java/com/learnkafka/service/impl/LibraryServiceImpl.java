package com.learnkafka.service.impl;

import com.learnkafka.api.model.LibraryEvent;
import com.learnkafka.api.model.LibraryEventType;
import com.learnkafka.generator.LibraryEventGenerator;
import com.learnkafka.producer.LibraryEventProducer;
import com.learnkafka.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private final LibraryEventProducer libraryEventProducer;

    @Override
    public String process(LibraryEvent libraryEvent, LibraryEventType libraryEventType) {
        libraryEvent.setLibraryEventType(libraryEventType);
        return libraryEventProducer.sendLibraryEvent(LibraryEventGenerator.createLibraryEvent(libraryEvent));
    }
}
