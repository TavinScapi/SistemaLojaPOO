package com.sistemaloja.model;

import java.time.LocalDateTime;

public class Cliente {
    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String endereco;
    private String observacoes;
    private boolean ativo = true;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;

    public Cliente(int id, String nome, String cpf, String email, String telefone,
                   String endereco, String observacoes, boolean ativo,
                   LocalDateTime dataCadastro, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.observacoes = observacoes;
        this.ativo = ativo;
        this.dataCadastro = dataCadastro;
        this.dataAtualizacao = dataAtualizacao;
    }

    public Cliente(String nome, String cpf, String email, String telefone,
                   String endereco, String observacoes) {
        this(0, nome, cpf, email, telefone, endereco, observacoes,
                true, null, null);
    }

    public Cliente(int id, String nome, String cpf, String email,
                   String telefone, String endereco, String observacoes) {
        this(id, nome, cpf, email, telefone, endereco, observacoes,
                true, null, null);
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public String getEndereco() { return endereco; }
    public String getObservacoes() { return observacoes; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }

    @Override
    public String toString() { return nome; }
}
