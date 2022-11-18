package com.gorsh.rednews.service;

import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.repository.PersonRepository;
import org.jvnet.hk2.annotations.Service;

@Service
public interface PersonService {

    public Person save(Person person);
}
