package com.learnkafka.api.controller;

import com.learnkafka.api.model.Book;
import com.learnkafka.api.model.LibraryEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryEventsControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void postLibraryEvent() {
        Book book = Book.builder()
                .id(123L)
                .author("TestAuthor")
                .name("TestName")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(123L)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders ();
        headers.set ("content-type", MediaType.APPLICATION_JSON. toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        ResponseEntity<LibraryEvent> response = restTemplate
                .exchange("/v1/libraryevent", HttpMethod.POST, request, LibraryEvent.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
