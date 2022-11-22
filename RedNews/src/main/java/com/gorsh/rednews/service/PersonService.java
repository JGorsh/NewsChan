package com.gorsh.rednews.service;

import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.repository.PersonRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class PersonService {

    PersonRepository personRepository;

    @Autowired
    public PersonService (PersonRepository personRepository){
        this.personRepository = personRepository;
    }

    public Person save(Person person) {
        return personRepository.save(person);
    }
}
