package com.learnkafka.service;

import com.learnkafka.api.model.LibraryEvent;
import com.learnkafka.api.model.LibraryEventType;

public interface LibraryService {
    String process(LibraryEvent libraryEvent, LibraryEventType libraryEventType);
}
