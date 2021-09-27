package com.example.inova.api.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.example.inova.api.model.Usuario;
import com.example.inova.api.model.UsuarioLogado;
import com.example.inova.api.repository.UsuarioLogadoRepository;
import com.example.inova.api.repository.UsuarioRepository;

@Service
public class UsuarioService {


	public UsuarioService(UsuarioRepository userRepository, UsuarioLogadoRepository userLogedRepository) {
		super();
		this.userRepository = userRepository;
		this.userLogedRepository = userLogedRepository;
	}


	private UsuarioRepository userRepository;
	private UsuarioLogadoRepository userLogedRepository;
	
	public UsuarioLogado checkLoginById(String chave) {
		return userLogedRepository.findByChave(chave);
	}
	
	// esta funcao utiliza a classe repositorio para pegar o usuarios pelo nome do login
	public Usuario loginUser(String name, String senha){
		Usuario encontrado = null;
		List<Usuario> user = userRepository.findByNome(name);
		for (Usuario usuario : user) {
			if(usuario.getSenha().equals(senha)) {
				System.out.println("useruario efetuando o login dentro da classe service  --> "+usuario.getNome());
				encontrado = usuario;
			}
		}
		
		return encontrado;
	
	}
	
	
	// esta funcao recebe os dados já como novo do fron-end e compara com o memsmo usuario no banco
	// fazendo com seja salvo apenas os dados novos mantendo todos os dados anteriores 
	// que não foram modificados.
	public Usuario evitaCamposVazios(Usuario novo, Usuario antigo) {
		if(novo.getCargo()==null || novo.getCargo().isBlank()) {
			novo.setCargo(antigo.getCargo());
		}
		if(novo.getNivel_acesso()<1) {
			novo.setNivel_acesso(antigo.getNivel_acesso());
		}
		if(novo.getMatricula()==0) {
			novo.setMatricula(antigo.getMatricula());
		}
		if(novo.getSetorID()==0) {
			novo.setSetorID(antigo.getSetorID());
		}
		if(novo.getNome().isBlank()|| novo.getNome()==null) {
			novo.setNome(antigo.getNome());
		}
		if(novo.getSenha().isBlank() || novo.getSenha()==null) {
			novo.setSenha(antigo.getSenha());
		}
		return novo;
	}
}
