package Model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class Jogador {
	private final String nome;
	private final String cor;
	private String herdeiro;
	private final Map<TipoTributo, Integer> recursos;
	private final List<Tesouro> tesouros;
	private final List<CartaConselheiro> cartasConselheiro;
	private final List<PecaMelhoria> melhorias;
	private int votos;

	Jogador(String nome, String cor, String herdeiro) {
		this.nome = validarTexto(nome, "O nome do jogador é obrigatório.");
		this.cor = validarTexto(cor, "A cor do jogador é obrigatória.");
		this.herdeiro = validarTexto(herdeiro, "O herdeiro é obrigatório.");
		this.recursos = new EnumMap<>(TipoTributo.class);
		this.tesouros = new ArrayList<>();
		this.cartasConselheiro = new ArrayList<>();
		this.melhorias = new ArrayList<>();
	}

	private static String validarTexto(String texto, String mensagemErro) {
		Objects.requireNonNull(texto, mensagemErro);
		if (texto.isBlank()) {
			throw new IllegalArgumentException(mensagemErro);
		}
		return texto;
	}

	String getNome() {
		return nome;
	}

	String getCor() {
		return cor;
	}

	String getHerdeiro() {
		return herdeiro;
	}

	void setHerdeiro(String herdeiro) {
		this.herdeiro = validarTexto(herdeiro, "O herdeiro é obrigatório.");
	}

	int getQuantidadeRecurso(TipoTributo tipo) {
		return recursos.getOrDefault(Objects.requireNonNull(tipo), 0);
	}

	int getYurts() {
		return getQuantidadeRecurso(TipoTributo.YURT);
	}

	Map<TipoTributo, Integer> getRecursos() {
		return Map.copyOf(recursos);
	}

	void adicionarTributo(PecaTributo tributo) {
		Objects.requireNonNull(tributo, "A peça de tributo é obrigatória.");
		adicionarRecursos(tributo.getTipo(), 1);
	}

	void adicionarRecursos(TipoTributo tipo, int quantidade) {
		Objects.requireNonNull(tipo, "O tipo do tributo é obrigatório.");
		validarQuantidade(quantidade);
		if (quantidade == 0) {
			return;
		}
		recursos.merge(tipo, quantidade, Math::addExact);
	}

	boolean removerRecursos(TipoTributo tipo, int quantidade) {
		Objects.requireNonNull(tipo, "O tipo do tributo é obrigatório.");
		validarQuantidade(quantidade);
		int disponivel = getQuantidadeRecurso(tipo);
		if (disponivel < quantidade) {
			return false;
		}
		if (disponivel == quantidade) {
			recursos.remove(tipo);
		} else {
			recursos.put(tipo, disponivel - quantidade);
		}
		return true;
	}

	private static void validarQuantidade(int quantidade) {
		if (quantidade < 0) {
			throw new IllegalArgumentException("A quantidade não pode ser negativa.");
		}
	}

	List<String> getNomesTesouros() {
		return tesouros.stream().map(Tesouro::getNome).toList();
	}

	List<Tesouro> getTesouros() {
		return List.copyOf(tesouros);
	}

	void adicionarTesouro(Tesouro tesouro) {
		tesouros.add(Objects.requireNonNull(tesouro, "O tesouro é obrigatório."));
	}

	boolean removerTesouro(Tesouro tesouro) {
		return tesouros.remove(Objects.requireNonNull(tesouro, "O tesouro é obrigatório."));
	}

	List<String> getNomesCartasConselheiro() {
		return cartasConselheiro.stream().map(CartaConselheiro::getNome).toList();
	}

	List<CartaConselheiro> getCartasConselheiro() {
		return List.copyOf(cartasConselheiro);
	}

	void adicionarCartaConselheiro(CartaConselheiro carta) {
		cartasConselheiro.add(Objects.requireNonNull(carta, "A carta é obrigatória."));
	}

	List<String> getNomesMelhorias() {
		return melhorias.stream().map(PecaMelhoria::getNome).toList();
	}

	List<PecaMelhoria> getMelhorias() {
		return List.copyOf(melhorias);
	}

	void adicionarMelhoria(PecaMelhoria melhoria) {
		melhorias.add(Objects.requireNonNull(melhoria, "A melhoria é obrigatória."));
	}

	int getVotos() {
		return votos;
	}

	void adicionarVotos(int quantidade) {
		validarQuantidade(quantidade);
		votos = Math.addExact(votos, quantidade);
	}

	boolean removerVotos(int quantidade) {
		validarQuantidade(quantidade);
		if (votos < quantidade) {
			return false;
		}
		votos -= quantidade;
		return true;
	}
}
