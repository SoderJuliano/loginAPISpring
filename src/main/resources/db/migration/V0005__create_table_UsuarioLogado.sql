create table usuario_logado(
	id bigint primary key not null auto_increment,
    id_usuario bigint not null,
    chave varchar(255) not null
);
ALTER TABLE `usuario_logado` ADD CONSTRAINT `fk_usuarioLogado` FOREIGN KEY ( `id_usuario` ) REFERENCES `usuario` ( `id` ) ;