package com.learnkafka.mapper;

import com.learnkafka.entity.LibraryEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LibraryEventMapper {
    LibraryEvent libraryEventToLibraryEventEntity(com.learnkafka.model.LibraryEvent libraryEvent);
}
