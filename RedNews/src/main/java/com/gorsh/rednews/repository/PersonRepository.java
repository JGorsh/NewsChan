package com.gorsh.rednews.repository;

import com.gorsh.rednews.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
}
