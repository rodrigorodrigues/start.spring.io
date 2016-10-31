package com.example.exception;

import com.example.entity.Person;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by rodrigo on 30/10/16.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(Person person) {
        super("This email '"+person.getEmail()+"' already exists");
    }
}
