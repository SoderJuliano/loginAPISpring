package com.example.inova.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.inova.api.model.Setor;

@Repository
public interface SetorRepository extends JpaRepository<Setor, Long>{

}
