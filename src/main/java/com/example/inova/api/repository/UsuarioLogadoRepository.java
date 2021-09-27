package com.example.inova.api.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.inova.api.model.UsuarioLogado;

@Repository
public interface UsuarioLogadoRepository extends JpaRepository<UsuarioLogado, Long>{
	UsuarioLogado findByChave(String chave);
}
