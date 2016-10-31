package com.example.controller;

import com.example.entity.Person;
import com.example.exception.UserNotFoundException;
import com.example.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by rodrigo on 12/10/16.
 */
@RestController
@RequestMapping("/persons")
public class PersonController implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    private PersonRepository personRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Person>> persons() {
        List<Person> persons = personRepository.findAll();
        persons.forEach(item -> item.add(
                linkTo(methodOn(PersonController.class).getPerson(item.getIdentification()))
                        .withSelfRel()));
        return ResponseEntity.ok(persons);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> save(@RequestBody @Validated Person person) {
        personRepository.save(person);
        generateHateoas(person);
        return ResponseEntity.created(null).body(person);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> update(@RequestBody Person person) throws Exception {
        ResponseEntity<Person> personOptional = getPerson(person.getIdentification());
        Person personFinder = personOptional.getBody();
        copyNonNullProperties(person, personFinder);
        personFinder.setEdit(true);
        personRepository.save(personFinder);
        return ResponseEntity.ok(personFinder);
    }

    private void generateHateoas(@RequestBody Person person) {
        person.add(linkTo(methodOn(PersonController.class)
                .getPerson(person.getIdentification()))
                .withSelfRel());
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        personRepository.delete(getPerson(id).getBody());
        return ResponseEntity.ok(true);
    }

    @DeleteMapping(value = "deleteMany/{ids}")
    public ResponseEntity<Boolean> deleteMany(@PathVariable("ids") List<String> ids) {
        System.out.println("ids: "+ids);
        ids.forEach(item ->
            personRepository.delete(getPerson(item).getBody())
        );
        return ResponseEntity.ok(true);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<Person> getPerson(@PathVariable String id) {
        ResponseEntity<Person> ok = ResponseEntity.ok(
                personRepository.findByIdentification(id)
                        .orElseThrow(() -> new UserNotFoundException(id)));
        generateHateoas(ok.getBody());
        return ok;
    }

    @Override
    public void run(String... strings) throws Exception {
        if (personRepository.count() == 0)
            personRepository.save(Arrays.asList(
                    new Person("Anna Ciolkowska", "anna@gmail.com"),
                    new Person("Rodrigo Rodrigues", "rodirgo@gmail.com")
            ));
    }

    protected void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
        logger.info(">>>>>>>>copyNonNullProperties:target: "+target);
    }

        @SuppressWarnings("rawtypes")
    protected String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            try {
                Object srcValue = src.getPropertyValue(pd.getName());
                if (srcValue == null) {
                    emptyNames.add(pd.getName());
                } else if (srcValue instanceof Collection && ((Collection) srcValue).isEmpty()) {
                    emptyNames.add(pd.getName());
                }
            } catch (NotReadablePropertyException ne) {}
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}

