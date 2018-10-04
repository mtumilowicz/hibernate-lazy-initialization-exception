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
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
        repository.findById(1).map(Employee::getIssues);
    }
    
    @Test(expected = LazyInitializationException.class)
    public void noSession_consumingCollection() {
        repository.findById(2).map(Employee::getIssues).map(Collection::size);
    }

    @Test
    @Transactional
    public void transactional() {
//        given
        Optional<Integer> collectionSizeOptional = repository.findById(3).map(Employee::getIssues).map(Collection::size);
        
//        expect
        assertThat(collectionSizeOptional.isPresent(), is(true));
        assertThat(collectionSizeOptional.get(), is(1));
    }
    
}
