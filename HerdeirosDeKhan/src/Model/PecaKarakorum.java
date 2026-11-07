package Model;

import java.util.ArrayList;
import java.util.List;

final class PecaKarakorum {
	static final List<String> TIPOS_TESOURO = List.of("gado", "especiaria", "pele", "ferro", "seda");

	private final List<String> tesourosExigidos;

	PecaKarakorum(List<String> tesourosExigidos) {
		if (tesourosExigidos == null || tesourosExigidos.size() != 3
				|| tesourosExigidos.stream().distinct().count() != 3
				|| !TIPOS_TESOURO.containsAll(tesourosExigidos))
			throw new IllegalArgumentException("A peca de Karakorum exige tres tesouros distintos e validos.");
		this.tesourosExigidos = new ArrayList<String>(tesourosExigidos);
	}

	List<String> getTesourosExigidos() {
		return new ArrayList<String>(tesourosExigidos);
	}
}
