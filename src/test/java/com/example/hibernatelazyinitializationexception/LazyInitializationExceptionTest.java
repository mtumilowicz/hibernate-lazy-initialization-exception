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

import java.util.Collection;

/**
 * Created by mtumilowicz on 2018-10-03.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LazyInitializationExceptionTest {
    
    @Autowired
    EmployeeRepository repository;

    @Test
    public void noSession_onlyGetter() {
        repository.save(Employee.builder().id(1).build()); // save in hibernate doesn't need session
        repository.findById(1).map(Employee::getIssues);
    }
    
    @Test(expected = LazyInitializationException.class)
    public void noSession_consumingCollection() {
        repository.save(Employee.builder().id(1).build());
        repository.findById(1).map(Employee::getIssues).map(Collection::size);
    }

    @Test
    @Transactional
    public void transactional() {
        repository.save(Employee.builder().id(1).build());
        System.out.println(repository.findById(1));
    }
    
}
