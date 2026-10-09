package Model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ModeloApi {
	private ModeloApi() {
		throw new AssertionError("Classe utilitária");
	}

	public static Jogador criarJogador(String nome, String cor, String herdeiro) {
		return new Jogador(nome, cor, herdeiro);
	}

	public static void adicionarTributo(Jogador jogador, TipoTributo tipo) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		jogador.adicionarTributo(new PecaTributo(tipo));
	}

	public static void adicionarRecursos(Jogador jogador, TipoTributo tipo, int quantidade) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		jogador.adicionarRecursos(tipo, quantidade);
	}

	public static boolean removerRecursos(Jogador jogador, TipoTributo tipo, int quantidade) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.removerRecursos(tipo, quantidade);
	}

	public static int getQuantidadeRecurso(Jogador jogador, TipoTributo tipo) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.getQuantidadeRecurso(tipo);
	}

	public static Map<TipoTributo, Integer> getRecursos(Jogador jogador) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.getRecursos();
	}

	public static int getYurts(Jogador jogador) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.getYurts();
	}

	public static void adicionarTesouro(Jogador jogador, String nome) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		jogador.adicionarTesouro(new Tesouro(nome));
	}

	public static List<String> getNomesTesouros(Jogador jogador) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.getNomesTesouros();
	}

	public static boolean removerTesouro(Jogador jogador, String nome) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.removerTesouro(new Tesouro(nome));
	}

	public static void adicionarCartaConselheiro(Jogador jogador, String nome, String condicao, String recompensa,
			boolean aberta) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		jogador.adicionarCartaConselheiro(new CartaConselheiro(nome, condicao, recompensa, aberta));
	}

	public static List<String> getNomesCartasConselheiro(Jogador jogador) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.getNomesCartasConselheiro();
	}

	public static void adicionarMelhoria(Jogador jogador, String nome, String efeito, String condicao) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		jogador.adicionarMelhoria(new PecaMelhoria(nome, efeito, condicao));
	}

	public static List<String> getNomesMelhorias(Jogador jogador) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.getNomesMelhorias();
	}

	public static int getVotos(Jogador jogador) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.getVotos();
	}

	public static void adicionarVotos(Jogador jogador, int quantidade) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		jogador.adicionarVotos(quantidade);
	}

	public static boolean removerVotos(Jogador jogador, int quantidade) {
		Objects.requireNonNull(jogador, "O jogador é obrigatório.");
		return jogador.removerVotos(quantidade);
	}
}
