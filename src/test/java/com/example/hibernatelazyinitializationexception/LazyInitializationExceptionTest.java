package com.example.hibernatelazyinitializationexception;

import com.example.hibernatelazyinitializationexception.entity.Employee;
import com.example.hibernatelazyinitializationexception.repository.EmployeeRepository;
import org.hibernate.LazyInitializationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by mtumilowicz on 2018-10-03.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LazyInitializationExceptionTest {
    
    @Autowired
    EmployeeRepository repository;
    
    @Test(expected = LazyInitializationException.class)
    public void noSession() {
        repository.save(Employee.builder().id(1).build()); // save in hibernate doesn't need session
        System.out.println(repository.findById(1));
    }

    @Test
    @Transactional
    public void transactional() {
        repository.save(Employee.builder().id(1).build()); // save in hibernate doesn't need session
        System.out.println(repository.findById(1));
    }
    
}
