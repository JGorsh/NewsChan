package com.gorsh.rednews.service;

import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonServiceImpl implements PersonService{

    @Autowired
    PersonRepository personRepository;
    @Override
    public Person save(Person person) {
        return personRepository.save(person);
    }
}
