package Model;

import java.util.Objects;

final class CartaConselheiro {
	private final String nome;
	private final String condicao;
	private final String recompensa;
	private boolean aberta;

	public CartaConselheiro(String nome, String condicao, String recompensa, boolean aberta) {
		this.nome = validarTexto(nome, "O nome da carta é obrigatório.");
		this.condicao = validarTexto(condicao, "A condição da carta é obrigatória.");
		this.recompensa = validarTexto(recompensa, "A recompensa da carta é obrigatória.");
		this.aberta = aberta;
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

	public String getCondicao() {
		return condicao;
	}

	public String getRecompensa() {
		return recompensa;
	}

	public boolean isAberta() {
		return aberta;
	}

	public void revelar() {
		aberta = true;
	}
}
