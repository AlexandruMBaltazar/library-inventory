package com.learnkafka.api.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibraryEvent {
    private Long libraryEventId;
    private Book book;
}
