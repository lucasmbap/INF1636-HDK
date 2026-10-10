package Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ModeloApi {
	private final Map<Integer, Jogador> jogadores = new HashMap<>();
	private int proximoIdJogador = 1;

	public int criarJogador(String nome, String cor, String herdeiro) {
		Jogador jogador = new Jogador(nome, cor, herdeiro);
		int id = proximoIdJogador;
		int proximoId = Math.incrementExact(proximoIdJogador);
		jogadores.put(id, jogador);
		proximoIdJogador = proximoId;
		return id;
	}

	public String getNomeJogador(int idJogador) {
		return obterJogador(idJogador).getNome();
	}

	public String getCorJogador(int idJogador) {
		return obterJogador(idJogador).getCor();
	}

	public String getHerdeiro(int idJogador) {
		return obterJogador(idJogador).getHerdeiro();
	}

	public void setHerdeiro(int idJogador, String herdeiro) {
		obterJogador(idJogador).setHerdeiro(herdeiro);
	}

	public void adicionarTributo(int idJogador, TipoTributo tipo) {
		obterJogador(idJogador).adicionarTributo(new PecaTributo(tipo));
	}

	public void adicionarRecursos(int idJogador, TipoTributo tipo, int quantidade) {
		obterJogador(idJogador).adicionarRecursos(tipo, quantidade);
	}

	public boolean removerRecursos(int idJogador, TipoTributo tipo, int quantidade) {
		return obterJogador(idJogador).removerRecursos(tipo, quantidade);
	}

	public int getQuantidadeRecurso(int idJogador, TipoTributo tipo) {
		return obterJogador(idJogador).getQuantidadeRecurso(tipo);
	}

	public Map<TipoTributo, Integer> getRecursos(int idJogador) {
		return obterJogador(idJogador).getRecursos();
	}

	public int getYurts(int idJogador) {
		return obterJogador(idJogador).getYurts();
	}

	public void adicionarTesouro(int idJogador, String nome) {
		obterJogador(idJogador).adicionarTesouro(new Tesouro(nome));
	}

	public List<String> getNomesTesouros(int idJogador) {
		return obterJogador(idJogador).getNomesTesouros();
	}

	public boolean removerTesouro(int idJogador, String nome) {
		Tesouro tesouro = new Tesouro(nome);
		return obterJogador(idJogador).removerTesouro(tesouro);
	}

	public void adicionarCartaConselheiro(int idJogador, String nome, String condicao, String recompensa,
			boolean aberta) {
		obterJogador(idJogador)
				.adicionarCartaConselheiro(new CartaConselheiro(nome, condicao, recompensa, aberta));
	}

	public List<String> getNomesCartasConselheiro(int idJogador) {
		return obterJogador(idJogador).getNomesCartasConselheiro();
	}

	public int getQuantidadeCartasConselheiro(int idJogador) {
		return obterJogador(idJogador).getCartasConselheiro().size();
	}

	public String getNomeCartaConselheiro(int idJogador, int indice) {
		return obterCartaConselheiro(idJogador, indice).getNome();
	}

	public String getCondicaoCartaConselheiro(int idJogador, int indice) {
		return obterCartaConselheiro(idJogador, indice).getCondicao();
	}

	public String getRecompensaCartaConselheiro(int idJogador, int indice) {
		return obterCartaConselheiro(idJogador, indice).getRecompensa();
	}

	public boolean isCartaConselheiroAberta(int idJogador, int indice) {
		return obterCartaConselheiro(idJogador, indice).isAberta();
	}

	public void revelarCartaConselheiro(int idJogador, int indice) {
		obterCartaConselheiro(idJogador, indice).revelar();
	}

	public void adicionarMelhoria(int idJogador, String nome, String efeito, String condicao) {
		obterJogador(idJogador).adicionarMelhoria(new PecaMelhoria(nome, efeito, condicao));
	}

	public List<String> getNomesMelhorias(int idJogador) {
		return obterJogador(idJogador).getNomesMelhorias();
	}

	public int getQuantidadeMelhorias(int idJogador) {
		return obterJogador(idJogador).getMelhorias().size();
	}

	public String getNomeMelhoria(int idJogador, int indice) {
		return obterMelhoria(idJogador, indice).getNome();
	}

	public String getEfeitoMelhoria(int idJogador, int indice) {
		return obterMelhoria(idJogador, indice).getEfeito();
	}

	public String getCondicaoMelhoria(int idJogador, int indice) {
		return obterMelhoria(idJogador, indice).getCondicao();
	}

	public int getVotos(int idJogador) {
		return obterJogador(idJogador).getVotos();
	}

	public void adicionarVotos(int idJogador, int quantidade) {
		obterJogador(idJogador).adicionarVotos(quantidade);
	}

	public boolean removerVotos(int idJogador, int quantidade) {
		return obterJogador(idJogador).removerVotos(quantidade);
	}

	private Jogador obterJogador(int idJogador) {
		Jogador jogador = jogadores.get(idJogador);
		if (jogador == null) {
			throw new IllegalArgumentException("Não existe jogador com o identificador " + idJogador + ".");
		}
		return jogador;
	}

	private CartaConselheiro obterCartaConselheiro(int idJogador, int indice) {
		return obterPorIndice(obterJogador(idJogador).getCartasConselheiro(), indice);
	}

	private PecaMelhoria obterMelhoria(int idJogador, int indice) {
		return obterPorIndice(obterJogador(idJogador).getMelhorias(), indice);
	}

	private static <T> T obterPorIndice(List<T> itens, int indice) {
		Objects.checkIndex(indice, itens.size());
		return itens.get(indice);
	}
}
