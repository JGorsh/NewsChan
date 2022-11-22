package com.gorsh.rednews.service;

import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class PersonService {

    @Autowired
    PersonRepository personRepository;
    

    public Person save(Person person) {
        return personRepository.save(person);
    }
}
