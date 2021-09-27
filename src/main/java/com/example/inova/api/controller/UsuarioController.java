package com.example.inova.api.controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import com.example.inova.api.model.Usuario;
import com.example.inova.api.model.UsuarioLogado;
import com.example.inova.api.repository.UsuarioLogadoRepository;
import com.example.inova.api.repository.UsuarioRepository;
import com.example.inova.api.service.UsuarioLogadoService;
import com.example.inova.api.service.UsuarioService;
import com.example.inova.api.common.Util;

@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
@RestController
@RequestMapping("/usuarios")
@SessionAttributes("sessaoid")
public class UsuarioController {

	@Autowired
	private UsuarioRepository usuariorepository;
	@Autowired
	private UsuarioService serviceUsuario;
	@Autowired
	private Util util;
	@Autowired
	private UsuarioLogadoRepository logadorepository;
	@Autowired
	private UsuarioLogadoService logadoService;


	public UsuarioController(UsuarioRepository usuariorepository, UsuarioService serviceUsuario, Util util,
			UsuarioLogadoRepository logadorepository) {
		super();
		this.usuariorepository = usuariorepository;
		this.serviceUsuario = serviceUsuario;
		this.util = util;
		this.logadorepository = logadorepository;
	}

	@GetMapping("/teste")
	public String setCookie(HttpServletResponse response) {
	    // create a cookie
	    Cookie cookie = new Cookie("username", "Jovan");

	    //add cookie to response
	    response.addCookie(cookie);

	    return "Username is changed!";
	}
	
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
	
	//@CrossOrigin(origins = "http://127.0.0.1:5500")
	@PostMapping("/login")
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Usuario> login(@Valid @RequestBody Usuario usuario, HttpServletRequest request, HttpServletResponse response) {
		
		Usuario userLogin = serviceUsuario.loginUser(usuario.getNome(), util.MD5(usuario.getSenha()));
		 LocalDateTime now = LocalDateTime.now();  
	        System.out.println("Before Formatting: " + now);  
	        DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy");  
	        String formatDateTime = now.format(format);  
	        System.out.println("After Formatting: " + formatDateTime); 
		if(userLogin == null) {
			return ResponseEntity.notFound().build();
		}else {
			Usuario usr = serviceUsuario.loginUser(userLogin.getNome(), userLogin.getSenha());
			HttpSession session = request.getSession();
			
			String chave = formatDateTime+""+userLogin.getSenha()+"-"+userLogin.getId();
			
			UsuarioLogado logado = new UsuarioLogado();
			logado.setChave(chave);
			System.out.println(usuario.getId()+" o id do ususario");
			logado.setId_usuario(usr.getId());
			session.setAttribute("usuarioLogado", userLogin);
			
			if(logadoService.isLoged(logado)==false) {
				logadorepository.save(logado); /// ver isso aqui tambem   <<--
				System.out.println(chave+" logado com sucesso -- the key");
			}else {
				System.out.println("ja consta nos registros");
			}
			
			Cookie c = new Cookie("InovaInd", chave);
			c.setMaxAge(60*60*24);
			c.setPath("/");
			response.addCookie(c);
			System.out.println("jsessionid  --> "+session.getId());
			return ResponseEntity.ok(userLogin);
			
		}
	}

	@RequestMapping("/index")
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Optional<Usuario>> index(@Valid HttpSession session, @RequestBody UsuarioLogado chave) {
		System.out.println(chave.getChave()+ "a chave que veio do front");
		UsuarioLogado logado = new UsuarioLogado();
		Optional<Usuario> user;
		System.out.println(session.getAttribute("usuarioLogado"));
		user = (Optional<Usuario>) session.getAttribute("usuarioLogado");
		Usuario u = new Usuario();
		if(user == null) {
			System.out.println("user nulo");
			logado = logadorepository.findByChave(chave.getChave());
			long id = logado.getId_usuario();
			user = usuariorepository.findById(id);
			
		}
		
		System.out.println(user.getClass().getName()+" usuario recuperado da sessao");
		return ResponseEntity.ok(user);
	}
	
	@RequestMapping
	@CrossOrigin(origins ="http://localhost/5500", originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public List<Usuario> listar(HttpSession session, @RequestBody UsuarioLogado chave){
		System.out.println("usuario logado fazendo requisição na api. Usuario: "+session.getAttributeNames());
		if (session.getAttribute("usuarioLogado")!=null) {
			System.out.println("user nao e nulo "+session.getAttribute("usuarioLogado").getClass());
			//session.removeAttribute("usuarioLogado");
			return usuariorepository.findAll();
		}else {
			if(logadoService.isLoged(chave)==false) {
				return (List<Usuario>) ResponseEntity.notFound().build();
			}else {
				return usuariorepository.findAll();
			}
		}
		
	}
	
	@PostMapping("/cadastrar")
	@ResponseStatus(HttpStatus.CREATED)
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public Usuario cadastrar(@Valid @RequestBody Usuario usuario) {
		return usuariorepository.save(usuario);
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
		
		user = serviceUsuario.evitaCamposVazios(user, u);
		
		user.setId(usuarioId);
		System.out.println("MD5"+user.getSenha());
		user.setSenha(util.MD5(user.getSenha()));
		user = usuariorepository.save(user);
		return ResponseEntity.ok(user);
	}
	
}


/*@CrossOrigin(origins = {"http://localhost:5500"},
methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT}, allowedHeaders = "true")
*/