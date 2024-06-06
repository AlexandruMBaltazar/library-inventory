package com.learnkafka.generator;

import com.learnkafka.model.Book;
import com.learnkafka.model.LibraryEvent;

public class LibraryEventGenerator {
    private LibraryEventGenerator() {
    }

    public static LibraryEvent createLibraryEvent(final com.learnkafka.api.model.LibraryEvent apiLibraryEvent) {
        return LibraryEvent.newBuilder()
                .setLibraryEventId(apiLibraryEvent.getLibraryEventId())
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
