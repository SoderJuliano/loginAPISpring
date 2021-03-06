package com.example.inova.api.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import com.example.inova.api.model.Usuario;
import com.example.inova.api.model.UsuarioLogado;
import com.example.inova.api.repository.UsuarioLogadoRepository;
import com.example.inova.api.repository.UsuarioRepository;
import com.example.inova.api.service.UsuarioLogadoService;
import com.example.inova.api.service.UsuarioService;

import lombok.AllArgsConstructor;

import com.example.inova.api.common.Util;

//libera o acesso ao navegador na origin escrita abaixo com os metodos mensionados
@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
@RestController
@RequestMapping("/usuarios")
@AllArgsConstructor
public class UsuarioController {

	//As classes repository usa recursos de busca na tabela
	//As classes service usa e aplica as regras de neg??cio
	
	@Autowired
	private UsuarioRepository usuariorepository;
	@Autowired
	private UsuarioService usuarioservice;
	@Autowired
	private Util util;
	@Autowired
	private UsuarioLogadoRepository logadorepository;
	@Autowired
	private UsuarioLogadoService logadoService;

	//um teste para retornar todos os cookies
	@GetMapping("/all")
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public String readAllCookies(HttpServletRequest request) {

	    Cookie[] cookies = request.getCookies();
	    if (cookies != null) {
	        return Arrays.stream(cookies)
	                .map(c -> c.getName() + "=" + c.getValue()).collect(Collectors.joining(", "));
	    }

	    return "No cookies";
	}

	//remove a sess??o no momento do logout
	@PostMapping("/logout")
	public void logout(HttpServletRequest request, @RequestBody UsuarioLogado logado) {
		//poderia fazer de duas maneuiras
		//PRIMEIRA -> atraves dos cookies usar um inovaind[0].split(-)[1] pra pegar o id do usuario e remover
		//SEGUNDA -> esta que eu apliquei abaixo, passei o cookie pelo RequestBody como um objeto
		Cookie[] cookies = request.getCookies();
		String invovaind = null;
		if(cookies!=null) {
			invovaind = Arrays.stream(cookies)
			.map(c -> c.getName() + "=" + c.getValue()).collect(Collectors.joining(", "));
			System.out.println("inovaind cookie --> "+invovaind);
		}
		if(logado!=null) {
			logadoService.removerDaSessao(logado);
		}
	}
	
	//faz login no sistema salvando o usu??rio no banco de dados
	@PostMapping("/login")
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Usuario> login(@Valid @RequestBody Usuario usuario, HttpServletRequest request, 
			HttpServletResponse response) {
		//cria um usu??rio para retonar na fun????o, passando como objeto, se ele for encontrado no banco com a fun????o loginUser
		Usuario userLogin = usuarioservice.loginUser(usuario.getNome(), util.MD5(usuario.getSenha()));
		//pega a data
		LocalDateTime now = LocalDateTime.now();  
		 
		//imprime a data no console
	    System.out.println("Before Formatting: " + now);  
	    
	    //formata a data e imprime
	    DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy");  
	    String formatDateTime = now.format(format);  
	        
	    System.out.println("After Formatting: " + formatDateTime); 
	     
	    //se o usu??rio foi encontrado, sen??o ele ?? nulo e volta uma resposta 404 n??o encontrado
		if(userLogin == null) {
			return ResponseEntity.notFound().build();
		}else {
			
			//Usuario usr = serviceUsuario.loginUser(userLogin.getNome(), userLogin.getSenha());
			
			//inicia uma sess??o JSessionId
			HttpSession session = request.getSession();
			session.setAttribute("usuarioLogado", userLogin);
			
			//cria uma chave para cookie
			String chave = formatDateTime+""+userLogin.getSenha()+"-"+userLogin.getId();
			
			//UsuarioLogado referente a sess??o no banco de dados
			UsuarioLogado logado = new UsuarioLogado();
			logado.setChave(chave);
			
			//coloca o id do userLogin no unu??rio logado para referencia-lo
			logado.setId_usuario(userLogin.getId());
			
			//isloged procura na db se j?? consta nos registros
			if(logadoService.isLoged(logado)==false) {
				// se o usu??rio ainda n??o est?? na db ele salva l??
				logadorepository.save(logado); 
				System.out.println(chave+" logado com sucesso -- the key");
			}else {
				//sen??o s?? imprime no console que n??o precisa salvar de novo
				System.out.println("ja consta nos registros");
			}
			
			//pega o header da requisi????o e seta o jsessionid com os atributos necess??rios para o navegador
			Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
			for (String header : headers) { // there can be multiple Set-Cookie attributes
	            boolean firstHeader = false;
				if (firstHeader) {
	                response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite=None"));
	                firstHeader = false;
	                continue;
	            }
				response.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "httpOnly; Secure=True; SameSite=None"));
			}
			
			//cria um cookie personalizado caso o jsessionid n??o funcione
			final ResponseCookie responseCookie = ResponseCookie
			        .from("InovaInd", chave)
			        .secure(true)
			        .httpOnly(true)
			        .path("/")
			        .maxAge(12345)
			        .sameSite("None")
			        .build();
			//responde com o cookie
			response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
			
			System.out.println("jsessionid  --> "+session.getId());
			
			//se ocorreu tudo certo responde com os dados do usu??rio
			return ResponseEntity.ok(userLogin);
			
		}
	}

	//funcao que valida a sessao
	@RequestMapping("/index")
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Usuario> index(@Valid HttpSession session, @RequestBody UsuarioLogado chave) {
		
		System.out.println(chave.getChave()+ " a chave que veio do front no m??todo de verefica????o de sess??o /index");
		
		Usuario user = null;
		
		if(session.getAttribute("usuarioLogado")!=null) {
			
			System.out.println(session.getAttribute("usuarioLogado")+" vereficando se existe um jsessionid");
			
			user = (Usuario) session.getAttribute("usuarioLogado");
		}
		
		
		if(user == null) {
			
			System.out.println("userario da sess??o nulo. Criaremos uma nova sess??o no banco de dados");
			UsuarioLogado logado = new UsuarioLogado();
			logado = logadorepository.findByChave(chave.getChave());
			long id = logado.getId_usuario();
			user = usuariorepository.findById(id).get();
			System.out.println("sess??o criada com o nome "+user.getNome());
			
		}
		
		System.out.println(user.getNome()+" usuario recuperado da sessao");
		return ResponseEntity.ok(user);
	}
	
	@RequestMapping
	@CrossOrigin(origins ="http://localhost/5500", originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public List<Usuario> listar(HttpSession session, @RequestBody UsuarioLogado chave){
		
		System.out.println("usuario logado fazendo requisi????o na api. Usuario: "+session.getAttributeNames());
		
		if (session.getAttribute("usuarioLogado")!=null) {
			System.out.println("user nao e nulo "+session.getAttribute("usuarioLogado").getClass());
			return usuariorepository.findAll();
		}else {
			if(logadoService.isLoged(chave)==false) {
				return null;
			}else {
				return usuariorepository.findAll();
			}
		}
		
	}
	
	
	@GetMapping("/acesso")
	public int tenhoAcesso(HttpSession session) {
		System.out.println("pedindo acesso... retornando o n??vel de acesso");
		Usuario user = (Usuario) session.getAttribute("usuarioLogado");
		if(user==null) {
			return -1;
		}
		return user.getNivel_acesso();
	}
	
	@PostMapping("/cadastrar")
	@ResponseStatus(HttpStatus.CREATED)
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public Usuario cadastrar(@Valid @RequestBody Usuario usuario, HttpSession session) throws Exception {
		return usuarioservice.salvar(usuario, session);
	}
	
	@RequestMapping("/{usuarioId}")
	@CrossOrigin(origins ="http://localhost/5500", originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Usuario> buscar(@PathVariable Long usuarioId, HttpSession session, @RequestBody UsuarioLogado chave){
		
		//cria um usuario para retorno
		Usuario user;
		System.out.println(session.getAttribute("usuarioLogado"));
		
		// busca na sessao (ver jsessionId) um usuario ativo
		user = (Usuario) session.getAttribute("usuarioLogado");
		
		//checa o usuario da sessao
		if(user == null) {
			System.out.println("user nulo");
			//busca da sessao do banco de dados
			user = logadoService.getUsuarioSessao(chave);
		}
		
		System.out.println(user.getClass().getName()+" usuario recuperado da sessao");
		
		//nao encontrou usuario erro 404 encontrou status 200 ok
		if(user==null) {
			return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
		}else {
			return usuariorepository.findById(usuarioId)
					.map(ResponseEntity::ok)
					.orElse(ResponseEntity.notFound().build());
		}
	}
	
	@DeleteMapping("/{usuarioId}")
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Long> deletar(@PathVariable Long usuarioId){
		 usuariorepository.deleteById(usuarioId);
		 if(!usuariorepository.findById(usuarioId).isPresent()) {
			 return new ResponseEntity<>(usuarioId, HttpStatus.OK);
		 }
		 return new ResponseEntity<>(HttpStatus.NOT_FOUND);	 
	}
	
	@PutMapping("{usuarioId}")
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Usuario> atualizar(@Valid @PathVariable Long usuarioId, @RequestBody Usuario user) throws Exception{
		
		if(!usuariorepository.existsById(usuarioId)) {
			return ResponseEntity.notFound().build();
		}
		
		Usuario u = new Usuario();
		u = usuariorepository.findById(usuarioId).get();
		
		user = usuarioservice.evitaCamposVazios(user, u);
		
		user.setId(usuarioId);
		System.out.println("MD5"+user.getSenha());
		user.setSenha(util.MD5(user.getSenha()));
		user = usuariorepository.save(user);
		return ResponseEntity.ok(user);
	}
	
}