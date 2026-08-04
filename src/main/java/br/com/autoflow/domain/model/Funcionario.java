package br.com.autoflow.domain.model;

import br.com.autoflow.domain.enums.Genero;

import java.time.LocalDate;
import java.util.UUID;

public class Funcionario {

    private UUID id;
    private String cpf;
    private String nome;
    private String telefone;
    private String email;
    private Genero genero;
    private LocalDate dataNascimento;
    private String cargo;
    private Endereco endereco;
}
