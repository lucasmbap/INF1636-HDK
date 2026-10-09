package Model;

import java.util.Objects;

final class PecaMelhoria {
	private final String nome;
	private final String efeito;
	private final String condicao;

	public PecaMelhoria(String nome, String efeito, String condicao) {
		this.nome = validarTexto(nome, "O nome da melhoria é obrigatório.");
		this.efeito = validarTexto(efeito, "O efeito da melhoria é obrigatório.");
		this.condicao = validarTexto(condicao, "A condição da melhoria é obrigatória.");
	}

	private static String validarTexto(String texto, String mensagemErro) {
		Objects.requireNonNull(texto, mensagemErro);
		if (texto.isBlank()) {
			throw new IllegalArgumentException(mensagemErro);
		}
		return texto;
	}

	public String getNome() {
		return nome;
	}

	public String getEfeito() {
		return efeito;
	}

	public String getCondicao() {
		return condicao;
	}
}
