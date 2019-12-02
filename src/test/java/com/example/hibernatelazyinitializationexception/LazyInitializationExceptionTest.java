package com.example.hibernatelazyinitializationexception;

import com.example.hibernatelazyinitializationexception.entity.Employee;
import com.example.hibernatelazyinitializationexception.entity.Issue;
import com.example.hibernatelazyinitializationexception.repository.EmployeeRepository;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    EntityManager entityManager;
    @Autowired
    TransactionTemplate transactionTemplate;

    @Test
    public void noSession_onlyGetter() {
        repository.findById(1).map(Employee::getIssues);
    }


    @Test(expected = LazyInitializationException.class)
    public void noSession_consumingCollection() {
        Collection<Issue> issues = repository.findById(2).get().getIssues();
        issues.size();
    }


    @Test
    public void manualSession_hibernateInitialize_consumingCollectionOutsideOfSession() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        //the entity manager i am using here, is not the one spring is using! So i must not use repository.findById();
        Employee employee = entityManager.find(Employee.class,2);
        Hibernate.initialize(employee.getIssues());

        entityManager.getTransaction().commit();
        entityManager.close();

        //Session closed now
        //I should be able to use getIssues now without Session/Transaction bc I used Hibernate.initialize(employee.getIssues());
        employee.getIssues().size();
    }

    @Test(expected = LazyInitializationException.class)
    public void manualSession_hibernateInitializeWholeEntity_consumingCollectionOutsideOfSession() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        //the entity manager i am using here, is not the one spring is using! So i must not use repository.findById();
        Employee employee = entityManager.find(Employee.class,2);
        //initializing whole Entity, not explicitly collection
        //wont work cause Hibernate.initialize() does not work recursively
        Hibernate.initialize(employee);

        entityManager.getTransaction().commit();
        entityManager.close();

        //Session closed now
        //I should be able to use getIssues now without Session/Transaction bc I used Hibernate.initialize(employee.getIssues());
        employee.getIssues().size();
    }


    @Test(expected = LazyInitializationException.class)
    public void manualSession_consumingCollectionOutsideOfSession() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        //the entity manager i am using here, is not the one spring is using! So i must not use repository.findById();
        Employee employee = entityManager.find(Employee.class,2);

        entityManager.getTransaction().commit();
        entityManager.close();
        //Session closed now
        //I should NOT be able to use getIssues now without Session/Transaction bc I did NOT used Hibernate.initialize(employee.getIssues());
        employee.getIssues().size();
    }

    @Test()
    public void manualSession_consumingCollection() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        Employee employee = entityManager.find(Employee.class,2);
        Collection<Issue> issues = employee.getIssues();
        issues.size();

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test(expected = IllegalStateException.class)
    public void manualTransactional_useInjectedEntityManager(){
        entityManager.getTransaction().begin();

        //Not allowed to create transaction on shared EntityManager - use Spring transactions or EJB CMT instead
        //Spring tells us to use its api for manual transaction handling and not just inject the entity manager and issue queries ourselfes
        Employee employee = repository.findById(2).get();
        Collection<Issue> issues = employee.getIssues();
        issues.size();

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    @Transactional
    public void transactional() {
        Employee employee = repository.findById(2).get();
        employee.getIssues().size();
    }


    @Test
    public void transactionTemplate_hibernateInitialize_consumingCollectionOutsideOfSession() {
        Employee employee = transactionTemplate.execute(new TransactionCallback<Employee>() {

            @Override
            public Employee doInTransaction(TransactionStatus status) {
                Employee employeeOptional = repository.findById(2).get();
                Hibernate.initialize(employeeOptional.getIssues());
                return employeeOptional;
            }
        });

        //Session is closed now, trying to access lazy fetched collection
        employee.getIssues().size();
    }

    @Test(expected = LazyInitializationException.class)
    public void transactionTemplate_consumingCollectionOutsideOfSession() {
        Employee employee = transactionTemplate.execute(new TransactionCallback<Employee>() {

            @Override
            public Employee doInTransaction(TransactionStatus status) {
                Employee employeeOptional = repository.findById(2).get();
                return employeeOptional;
            }
        });

        //Session is closed now, trying to access lazy fetched collection
        employee.getIssues().size();
    }

    @Test(expected = LazyInitializationException.class)
    public void externalTransactionalAnnotatedMethod_hibernateInitialize_consumingCollectionOutsideOfSession() {
        //Does not work, idk why
        Employee employee = loadEmployeeAndInitializeCollection(2);

        //Session is closed now, trying to access lazy fetched collection
        employee.getIssues().size();
    }


    @Transactional
    public Employee loadEmployeeAndInitializeCollection(Integer id){
        Employee employee = repository.findById(id).get();
        Hibernate.initialize(employee.getIssues());
        return employee;
    }


    @Test
    public void externalTransactionTemplateMethod_hibernateInitialize_consumingCollectionOutsideOfSession() {
        Employee employee = loadEmployeeAndInitializeCollection_withTransactionalTemplate(2);

        //Session is closed now, trying to access lazy fetched collection
        employee.getIssues().size();
    }

    public Employee loadEmployeeAndInitializeCollection_withTransactionalTemplate(Integer id){

        return transactionTemplate.execute(new TransactionCallback<Employee>() {

            @Override
            public Employee doInTransaction(TransactionStatus status) {
                Employee employee = repository.findById(id).get();
                Hibernate.initialize(employee.getIssues());
                return employee;
            }
        });
    }

    
}
