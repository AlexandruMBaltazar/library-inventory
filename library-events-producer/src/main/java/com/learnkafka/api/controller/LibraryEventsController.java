package com.learnkafka.api.controller;

import com.learnkafka.api.model.LibraryEvent;
import com.learnkafka.api.model.LibraryEventType;
import com.learnkafka.service.LibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LibraryEventsController {

    private final LibraryService libraryService;

    @PostMapping("v1/libraryevent")
    public ResponseEntity<Void> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) {
        libraryService.process(libraryEvent, LibraryEventType.NEW);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
