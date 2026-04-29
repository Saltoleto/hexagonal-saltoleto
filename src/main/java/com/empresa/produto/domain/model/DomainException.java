package com.empresa.produto.domain.model;

/**
 * Exceção de domínio — representa violação de regra de negócio.
 * Não carrega detalhes de infraestrutura (sem stack trace técnico ao cliente).
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
