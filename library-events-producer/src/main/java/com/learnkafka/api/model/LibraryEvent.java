package com.learnkafka.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LibraryEvent(
        Long libraryEventId,
        LibraryEventType libraryEventType,
        @NotNull
        @Valid
        Book book
) {
        public LibraryEvent withLibraryEventType(LibraryEventType newLibraryEventType) {
                return new LibraryEvent(this.libraryEventId, newLibraryEventType, this.book);
        }
}
