package com.learnkafka.service;

import com.learnkafka.api.model.LibraryEvent;

public interface LibraryService {
    String process(LibraryEvent libraryEvent);
}
