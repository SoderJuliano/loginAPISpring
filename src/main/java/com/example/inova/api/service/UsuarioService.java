package com.example.inova.api.service;


import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inova.api.common.Util;
import com.example.inova.api.model.Usuario;
import com.example.inova.api.model.UsuarioLogado;
import com.example.inova.api.repository.UsuarioLogadoRepository;
import com.example.inova.api.repository.UsuarioRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioService {

	private UsuarioRepository userRepository;
	private UsuarioLogadoRepository userLogedRepository;
	@Autowired
	private Util util;
	
	public UsuarioLogado checkLoginById(String chave) {
		return userLogedRepository.findByChave(chave);
	}
	
	@Transactional
	public Usuario salvar(Usuario user, HttpSession session) throws Exception {
		boolean nomeEmUso = userRepository.findByNome(user.getNome())
				.stream()
				.anyMatch(usuarioExistente -> !usuarioExistente.equals(user));
		
		if(nomeEmUso) {
			throw new Exception("Já existe um cliente cadastrado com este nome");
		}
		 Usuario local = (Usuario) session.getAttribute("usuarioLogado");
		if(local.getNivel_acesso()<user.getNivel_acesso()) {
			throw new Exception("Não pode cadastrar um usuário com nível maior que o seu");
		}
		user.setSenha(util.MD5(user.getSenha()));
		return userRepository.save(user);
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
		System.out.println("retornando "+encontrado.getNome());
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
