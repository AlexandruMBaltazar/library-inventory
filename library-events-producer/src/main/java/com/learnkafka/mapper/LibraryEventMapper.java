package com.learnkafka.mapper;

import com.learnkafka.api.model.LibraryEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LibraryEventMapper {
    com.learnkafka.model.LibraryEvent apiLibraryEventToLibraryEvent(LibraryEvent libraryEvent);
}
