package Model;

import java.util.Objects;

final class PecaTributo {
	private final TipoTributo tipo;

	public PecaTributo(TipoTributo tipo) {
		this.tipo = Objects.requireNonNull(tipo, "O tipo do tributo é obrigatório.");
	}

	public TipoTributo getTipo() {
		return tipo;
	}
}
