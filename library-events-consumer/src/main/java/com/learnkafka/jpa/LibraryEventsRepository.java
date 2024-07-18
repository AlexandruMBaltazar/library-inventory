package com.learnkafka.jpa;

import com.learnkafka.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Long> {
    boolean existsByLibraryEventId(Long libraryEventId);
}
