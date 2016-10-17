package com.example.controller;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by rodrigo on 12/10/16.
 */
@RestController
@RequestMapping("/persons")
public class PersonController implements CommandLineRunner {

    @Autowired
    private PersonRepository personRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Person>> persons() {
        return ResponseEntity.ok(personRepository.findAll());
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> save(@RequestBody @Validated Person person) {
        personRepository.save(person);
        return ResponseEntity.created(URI.create("self"))
                .body(person);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Person> update(@RequestBody Person person) {
        ResponseEntity<Person> personOptional = getPerson(person.getId());
        Person personFinder = personOptional.getBody();
        personFinder.setName(person.getName());
        personFinder.setEdit(true);
        personRepository.save(personFinder);
        return ResponseEntity.ok(personFinder);
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
        return ResponseEntity.ok(
                personRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Override
    public void run(String... strings) throws Exception {
        if (personRepository.count() == 0)
            personRepository.save(Arrays.asList(
                    new Person("Anna Ciolkowska", "anna@gmail.com"),
                    new Person("Rodrigo Rodrigues", "rodirgo@gmail.com")
            ));
    }
}

@Controller
class RedirectController {
    @RequestMapping("/")
    public String home() {
        return "redirect:/index.html";
    }
}

@Repository
interface PersonRepository extends MongoRepository<Person, String> {
    Optional<Person> findById(String id);
    Optional<Person> findByEmail(String id);

    @Override
    default Person save(Person person) {
        if (!person.getEdit() && findByEmail(person.getEmail()).isPresent())
            throw new EmailAlreadyExistsException(person);

        save(Arrays.asList(person));
        return person;
    }
}

@Document
class Person {
    @Id
    private String id;
    @NotBlank(message = "error.name.notblank")
    @Size(min = 10, max = 30, message = "error.name.size")
    private String name;
    @NotBlank(message = "error.email.notblank")
    @Email(message = "error.email.email")
    private String email;
    @Transient
    private Boolean edit = false;

    public Person() {}

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Person{" +
                "email='" + email + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("could not find user with id '" + userId + "'.");
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(Person person) {
        super("This email '"+person.getEmail()+"' already exists");
    }
}

@Configuration
class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CsrfTokenResponseHeaderBindingFilter csrfFilter = csrfTokenResponseHeaderBindingFilter();

        http
                .addFilterAfter(csrfFilter, CsrfFilter.class)
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/persons/**")
                        .permitAll()
                    .antMatchers(
                            "/common/**",
                            "/css/**",
                            "/js/**",
                            "/views/login_view.html")
                        .permitAll()
                .anyRequest()
                    .authenticated()
            .and().formLogin()
                .loginProcessingUrl("/j_spring_security_check")
                .usernameParameter("j_username")
                .passwordParameter("j_password")
            .and()
                .logout()
                .logoutUrl("/logout");
    }

    @Bean
    public CsrfTokenResponseHeaderBindingFilter csrfTokenResponseHeaderBindingFilter() {
        return new CsrfTokenResponseHeaderBindingFilter();
    }
}

class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {
    protected static final String REQUEST_ATTRIBUTE_NAME = "_csrf";
    protected static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
    protected static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
    protected static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, javax.servlet.FilterChain filterChain) throws ServletException, IOException {
        CsrfToken token = (CsrfToken) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
        if (token != null) {
            response.setHeader(RESPONSE_HEADER_NAME, token.getHeaderName());
            response.setHeader(RESPONSE_PARAM_NAME, token.getParameterName());
            response.setHeader(RESPONSE_TOKEN_NAME , token.getToken());
        }
        filterChain.doFilter(request, response);
    }
}