package com.example.hibernatelazyinitializationexception.repository;

import com.example.hibernatelazyinitializationexception.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by mtumilowicz on 2018-10-03.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
