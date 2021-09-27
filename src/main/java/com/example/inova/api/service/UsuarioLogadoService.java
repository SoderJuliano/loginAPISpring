package com.example.inova.api.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor;

import com.example.inova.api.model.Usuario;
import com.example.inova.api.model.UsuarioLogado;
import com.example.inova.api.repository.UsuarioLogadoRepository;
import com.example.inova.api.repository.UsuarioRepository;

@Service
public class UsuarioLogadoService {

	
	
	public UsuarioLogadoService(UsuarioLogadoRepository usuariologadorepository) {
		super();
		this.usuariologadorepository = usuariologadorepository;
	}

	private UsuarioLogadoRepository usuariologadorepository;
	private UsuarioRepository usuariorepository;
	
	
	//verifica se o usuario ja esta no banco para evitar uma entrada dupla
	public boolean isLoged(UsuarioLogado usuario) {
		if(usuariologadorepository.findByChave(usuario.getChave())!=null) {
			return true;
		}else {
			return false;
		}
	}
	
	// pega os dados e converte um usuario logado para uma entidade usuario usando a fk_usuario
	public Usuario getUsuarioSessao(UsuarioLogado ul) {
		System.out.println(ul.getChave()+ "a chave que veio do front");
		UsuarioLogado logado = new UsuarioLogado();
		Optional<Usuario> user;
		Usuario u = new Usuario();
		System.out.println("user nulo");
		logado = usuariologadorepository.findByChave(ul.getChave());
		long id = logado.getId_usuario();
		user = usuariorepository.findById(id);
		return user.get();
	}
}
