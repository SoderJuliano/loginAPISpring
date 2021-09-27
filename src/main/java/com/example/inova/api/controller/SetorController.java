package com.example.inova.api.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.inova.api.model.Setor;
import com.example.inova.api.model.Usuario;
import com.example.inova.api.repository.SetorRepository;
import com.example.inova.api.repository.UsuarioRepository;

@RestController
@RequestMapping("/setor")
public class SetorController {

	private SetorRepository setorRepository;
	private UsuarioRepository usuarioRepository;

	
	
	public SetorController(SetorRepository setorRepository, UsuarioRepository usuarioRepository) {
		super();
		this.setorRepository = setorRepository;
		this.usuarioRepository = usuarioRepository;
	}

	// lista os setores
	@GetMapping
	public List<Setor> listar(){
		return setorRepository.findAll();
	}
	
	
	// aqui voce pode passar o id do usuario que ele busca o setor dele
	@SuppressWarnings("deprecation")
	@GetMapping("/usuario/{usuarioId}")
	public ResponseEntity<Setor> buscarPorUsuario(@PathVariable Long usuarioId){
		Optional<Usuario> u = usuarioRepository.findById(usuarioId);
		return setorRepository.findById(new Long(u.get().getSetorID()))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	@GetMapping("/{setorId}")
	public ResponseEntity<Setor> buscar(@PathVariable Long setorId){
		return setorRepository.findById(setorId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
}
