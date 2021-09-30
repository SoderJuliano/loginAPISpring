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
@SessionAttributes("sessaoid")
@AllArgsConstructor
public class UsuarioController {

	//As classes repository usa recursos de busca na tabela
	//As classes service usa e aplica as regras de negócio
	
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

	//faz login no sistema salvando o usuário no banco de dados
	@PostMapping("/login")
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Usuario> login(@Valid @RequestBody Usuario usuario, HttpServletRequest request, 
			HttpServletResponse response) {
		//cria um usuário para retonar na função, passando como objeto, se ele for encontrado no banco com a função loginUser
		Usuario userLogin = usuarioservice.loginUser(usuario.getNome(), util.MD5(usuario.getSenha()));
		//pega a data
		LocalDateTime now = LocalDateTime.now();  
		 
		//imprime a data no console
	    System.out.println("Before Formatting: " + now);  
	    
	    //formata a data e imprime
	    DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy");  
	    String formatDateTime = now.format(format);  
	        
	    System.out.println("After Formatting: " + formatDateTime); 
	     
	    //se o usuário foi encontrado, senão ele é nulo e volta uma resposta 404 não encontrado
		if(userLogin == null) {
			return ResponseEntity.notFound().build();
		}else {
			
			//Usuario usr = serviceUsuario.loginUser(userLogin.getNome(), userLogin.getSenha());
			
			//inicia uma sessão JSessionId
			HttpSession session = request.getSession();
			session.setAttribute("usuarioLogado", userLogin);
			
			//cria uma chave para cookie
			String chave = formatDateTime+""+userLogin.getSenha()+"-"+userLogin.getId();
			
			//UsuarioLogado referente a sessão no banco de dados
			UsuarioLogado logado = new UsuarioLogado();
			logado.setChave(chave);
			
			//coloca o id do userLogin no unuário logado para referencia-lo
			logado.setId_usuario(userLogin.getId());
			
			
			
			Enumeration<String> list =  session.getAttributeNames();

			System.out.println(list.nextElement()+" next");
			
			
			//isloged procura na db se já consta nos registros
			if(logadoService.isLoged(logado)==false) {
				// se o usuário ainda não está na db ele salva lá
				logadorepository.save(logado); 
				System.out.println(chave+" logado com sucesso -- the key");
			}else {
				//senão só imprime no console que não precisa salvar de novo
				System.out.println("ja consta nos registros");
			}
			
			//pega o header da requisição e seta o jsessionid com os atributos necessários para o navegador
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
			
			//cria um cookie personalizado caso o jsessionid não funcione
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
			
			//se ocorreu tudo certo responde com os dados do usuário
			return ResponseEntity.ok(userLogin);
			
		}
	}

	//funcao que valida a sessao
	@RequestMapping("/index")
	@CrossOrigin(origins = {"http://localhost/5500", "x-requested-with", "content-type"}, originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", 
	methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public ResponseEntity<Usuario> index(@Valid HttpSession session, @RequestBody UsuarioLogado chave) {
		
		System.out.println(chave.getChave()+ " a chave que veio do front no método de vereficação de sessão /index");
		
		Usuario user = null;
		
		if(session.getAttribute("usuarioLogado")!=null) {
			
			System.out.println(session.getAttribute("usuarioLogado")+" vereficando se existe um jsessionid");
			
			user = (Usuario) session.getAttribute("usuarioLogado");
		}
		
		
		if(user == null) {
			
			System.out.println("userario da sessão nulo. Criaremos uma nova sessão no banco de dados");
			UsuarioLogado logado = new UsuarioLogado();
			logado = logadorepository.findByChave(chave.getChave());
			long id = logado.getId_usuario();
			user = usuariorepository.findById(id).get();
			System.out.println("sessão criada com o nome "+user.getNome());
			
		}
		
		System.out.println(user.getNome()+" usuario recuperado da sessao");
		return ResponseEntity.ok(user);
	}
	
	@RequestMapping
	@CrossOrigin(origins ="http://localhost/5500", originPatterns = "*", allowCredentials = "true", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
	public List<Usuario> listar(HttpSession session, @RequestBody UsuarioLogado chave){
		
		System.out.println("usuario logado fazendo requisição na api. Usuario: "+session.getAttributeNames());
		
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
		System.out.println("pedindo acesso... retornando o nível de acesso");
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