package com.velvet.sakura.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.velvet.sakura.entity.Account;
import java.util.List;


public interface AccountRepository extends JpaRepository<Account,Long> {
List<Account> findByName(String name);
boolean existsByName(String name);
boolean existsByEmail(String email);
    
}
