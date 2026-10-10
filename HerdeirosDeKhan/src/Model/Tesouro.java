package Model;

import java.util.Objects;

final class Tesouro {
	private final String nome;

	public Tesouro(String nome) {
		this.nome = Objects.requireNonNull(nome, "O nome do tesouro é obrigatório.");
		if (nome.isBlank()) {
			throw new IllegalArgumentException("O nome do tesouro não pode estar vazio.");
		}
	}

	public String getNome() {
		return nome;
	}

	@Override
	public boolean equals(Object outro) {
		if (this == outro) {
			return true;
		}
		if (!(outro instanceof Tesouro tesouro)) {
			return false;
		}
		return nome.equals(tesouro.nome);
	}

	@Override
	public int hashCode() {
		return nome.hashCode();
	}
}
