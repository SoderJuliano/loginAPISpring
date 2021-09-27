create table usuario(
	id bigint not null primary key auto_increment,
    nome varchar(100) not null,
    matricula int not null,
    cargo varchar(100),
    setor int
);