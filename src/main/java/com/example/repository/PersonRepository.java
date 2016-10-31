package com.example.repository;

import com.example.entity.Person;
import com.example.exception.EmailAlreadyExistsException;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by rodrigo on 30/10/16.
 */
@Repository
public interface PersonRepository extends MongoRepository<Person, String> {
    Optional<Person> findByIdentification(String id);
    Optional<Person> findByEmail(String id);

    @Override
    default Person save(Person person) {
        if (!person.getEdit() && findByEmail(person.getEmail()).isPresent())
            throw new EmailAlreadyExistsException(person);

        save(Arrays.asList(person));
        return person;
    }
}
