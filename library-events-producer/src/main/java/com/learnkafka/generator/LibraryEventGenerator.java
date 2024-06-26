package com.learnkafka.generator;

import com.learnkafka.model.Book;
import com.learnkafka.model.LibraryEvent;
import com.learnkafka.model.LibraryEventType;

public class LibraryEventGenerator {
    private LibraryEventGenerator() {
    }

    public static LibraryEvent createLibraryEvent(final com.learnkafka.api.model.LibraryEvent apiLibraryEvent) {
        return LibraryEvent.newBuilder()
                .setLibraryEventId(apiLibraryEvent.getLibraryEventId())
                .setLibraryEventType(LibraryEventType.valueOf(apiLibraryEvent.getLibraryEventType().toString()))
                .setBook(createBook(apiLibraryEvent.getBook()))
                .build();
    }

    private static Book createBook(final com.learnkafka.api.model.Book apiBook) {
        return Book.newBuilder()
                .setId(apiBook.getId())
                .setName(apiBook.getName())
                .setAuthor(apiBook.getAuthor())
                .build();
    }
}
