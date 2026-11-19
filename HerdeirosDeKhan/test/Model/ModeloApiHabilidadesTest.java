package Model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes das habilidades especiais dos herdeiros (texto das cartelas e
 * manual, pag. 20). Cada teste cria a partida com os herdeiros que usa:
 * o primeiro jogador e sempre Ana (vermelho), que comeca jogando.
 */
public class ModeloApiHabilidadesTest {
	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ANA = "Vermelho";
	private static final String BIA = "Azul";
	private static final String KARAKORUM = "Karakorum";

	private ModeloApi api;
	private int idAna;

	@Before
	public void pegaAApi() {
		api = ModeloApi.getInstance();
	}

	// ----------------------------------------------------------------- Altani

	@Test(timeout = DEFAULT_TIMEOUT)
	public void altani_comecaComDoisPeoesEmKarakorum() {
		partida("Altani", "Tolui");
		assertEquals("2o peao da Altani em Karakorum", KARAKORUM, api.getPosicaoSegundoPeao(ANA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void outroHerdeiro_naoTemSegundoPeao() {
		partida("Altani", "Tolui");
		assertNull("So a Altani tem 2 peoes", api.getPosicaoSegundoPeao(BIA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void altaniEscolheSegundoPeao_soEleSeMove() {
		partida("Altani", "Tolui");
		api.escolherPeao(2);
		api.ativarColuna(ModeloApi.COLUNA_VERDE); // Altani: 2 cavalos e 1 punho
		api.moverPara("R9");
		assertEquals("O 2o peao andou", "R9", api.getPosicaoSegundoPeao(ANA));
		assertEquals("O 1o peao ficou parado", KARAKORUM, api.getPosicao(ANA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void altani_naoTrocaDePeaoDepoisDeAtivarColuna() {
		partida("Altani", "Tolui");
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		assertFalse("A escolha do peao e antes da coluna", api.escolherPeao(2));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void outroHerdeiro_naoEscolhePeao() {
		partida("Tolui", "Altani");
		assertFalse("Tolui tem um peao so", api.escolherPeao(2));
	}

	// --------------------------------------------------------------- Chagatai

	@Test(timeout = DEFAULT_TIMEOUT)
	public void chagatai_trocaCavaloPorEspada() {
		partida("Chagatai", "Tolui");
		api.ativarColuna(ModeloApi.COLUNA_AMARELA); // Chagatai: 2 cavalos e 1 punho
		api.trocarCavaloPorEspada();
		assertEquals("Ganhou 1 peca de espada", 1, api.getQuantidadeRecurso(idAna, TipoTributo.ESPADA));
		assertEquals("Ficou 1 movimento", 1, api.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void chagatai_soUmaVezPorTurno() {
		partida("Chagatai", "Tolui");
		api.ativarColuna(ModeloApi.COLUNA_AMARELA);
		api.trocarCavaloPorEspada();
		assertFalse("1x por turno", api.trocarCavaloPorEspada());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void chagatai_semMovimento_recusado() {
		partida("Chagatai", "Tolui");
		assertFalse("Antes da coluna nao ha cavalo para trocar", api.trocarCavaloPorEspada());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void chagatai_podeUsarDeNovoNoTurnoSeguinte() {
		partida("Chagatai", "Tolui");
		api.ativarColuna(ModeloApi.COLUNA_AMARELA);
		api.trocarCavaloPorEspada();
		passarRodada();
		api.ativarColuna(ModeloApi.COLUNA_VERDE); // Chagatai: 1 cavalo, 1 punho e moeda
		assertTrue("Novo turno, nova troca", api.trocarCavaloPorEspada());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void outroHerdeiro_naoTrocaCavaloPorEspada() {
		partida("Tolui", "Chagatai");
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		assertFalse("So o Chagatai", api.trocarCavaloPorEspada());
	}

	// ----------------------------------------------------------------- Ogedei

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ogedei_pagaMoedaEPegaTesouroDeQualquerCidade() {
		partida("Ogedei", "Tolui");
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 1);
		String cidade = api.getCidadesDisponiveis().get(0);
		String tesouro = api.getTesourosDaCidade(cidade).get(0);
		assertTrue("Pega sem precisar estar ao lado da cidade", api.pegarTesouroComMoeda(cidade, tesouro));
		assertEquals("O tesouro e do Ogedei", tesouro, api.getNomesTesouros(idAna).get(0));
		assertEquals("Pagou 1 moeda", 0, api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ogedei_soUmaVezPorTurno() {
		partida("Ogedei", "Tolui");
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 2);
		String cidade = api.getCidadesDisponiveis().get(0);
		api.pegarTesouroComMoeda(cidade, api.getTesourosDaCidade(cidade).get(0));
		assertFalse("1x por turno", api.pegarTesouroComMoeda(cidade, api.getTesourosDaCidade(cidade).get(0)));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ogedei_semMoeda_recusado() {
		partida("Ogedei", "Tolui");
		String cidade = api.getCidadesDisponiveis().get(0);
		assertFalse("Precisa de 1 moeda", api.pegarTesouroComMoeda(cidade, api.getTesourosDaCidade(cidade).get(0)));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ogedei_naoPegaOUltimoTesouroDaCidade() {
		partida("Ogedei", "Tolui");
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 4);
		String cidade = api.getCidadesDisponiveis().get(0);
		for (int turno = 0; turno < 3; turno++) { // 4 tesouros -> 1
			api.pegarTesouroComMoeda(cidade, api.getTesourosDaCidade(cidade).get(0));
			passarRodada();
		}
		assertFalse("O ultimo tesouro da cidade nao", api.pegarTesouroComMoeda(cidade,
				api.getTesourosDaCidade(cidade).get(0)));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void outroHerdeiro_naoPegaTesouroComMoeda() {
		partida("Tolui", "Ogedei");
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 1);
		String cidade = api.getCidadesDisponiveis().get(0);
		assertFalse("So o Ogedei", api.pegarTesouroComMoeda(cidade, api.getTesourosDaCidade(cidade).get(0)));
	}

	// ------------------------------------------------------------------ Tolui

	@Test(timeout = DEFAULT_TIMEOUT)
	public void tolui_comecaComYurtEmKarakorum() {
		partida("Tolui", "Ogedei");
		assertEquals("Yurt inicial do Tolui", List.of(ANA), api.getYurtsNaParada(KARAKORUM));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void tolui_constroiYurtNaParadaAoLado() {
		partida("Tolui", "Ogedei");
		api.adicionarRecursos(idAna, TipoTributo.YURT, 1);
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		assertTrue("R9 e adjacente a Karakorum", api.construirYurt("R9"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void outroHerdeiro_naoConstroiYurtSoAoLado() {
		partida("Ogedei", "Tolui");
		api.adicionarRecursos(idAna, TipoTributo.YURT, 1);
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		assertFalse("Sem ter passado por R9", api.construirYurt("R9"));
	}

	// ------------------------------------------------------------------ Jochi

	@Test(timeout = DEFAULT_TIMEOUT)
	public void jochi_comecaComDuasMelhoriasParaEscolher() {
		partida("Jochi", "Tolui");
		assertEquals("2 melhorias tiradas ao acaso", 2, api.getMelhoriasIniciaisJochi(ANA).size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void jochi_escolhida_vaiParaColunaBranca() {
		partida("Jochi", "Tolui");
		String escolhida = api.getMelhoriasIniciaisJochi(ANA).get(1);
		api.escolherMelhoriaInicial(ANA, 1);
		assertEquals("Na coluna branca", List.of(escolhida), api.getMelhoriasDaColuna(ANA, ModeloApi.COLUNA_BRANCA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void jochi_aOutraVoltaParaAPilha() {
		partida("Jochi", "Tolui");
		api.escolherMelhoriaInicial(ANA, 0);
		assertEquals("39 - 2 + 1 devolvida", 38, api.getMelhoriasNaPilha());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void jochi_soEscolheUmaVez() {
		partida("Jochi", "Tolui");
		api.escolherMelhoriaInicial(ANA, 0);
		assertFalse("A escolha e so no inicio", api.escolherMelhoriaInicial(ANA, 0));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void jochi_compraParaColunaDeOutraCor() {
		partida("Jochi", "Tolui");
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 2);
		int outra = (api.getCorMelhoriaNaArea(4) + 1) % 3;
		assertTrue("Jochi nao precisa seguir a cor", api.comprarMelhoria(4, outra));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void jochi_linhaCompletaNaoDaOutraMelhoriaGratis() {
		Cartela jochi = new Cartela("Jochi");
		jochi.colocarMelhoriaInicial(TipoMelhoria.MOEDA);
		jochi.adicionarMelhoria(ColunaCartela.VERMELHA, TipoMelhoria.ESPADA);
		jochi.adicionarMelhoria(ColunaCartela.VERDE, TipoMelhoria.YURT);
		jochi.adicionarMelhoria(ColunaCartela.AMARELA, TipoMelhoria.PUNHO);
		assertEquals("O espaco da 1a linha na branca ja esta ocupado", 0, jochi.getMelhoriasGratisPendentes());
	}

	// -------------------------------------------------------------- auxiliares

	private void partida(String herdeiroAna, String herdeiroBia) {
		api.novaPartida();
		idAna = api.criarJogador("Ana", ANA, herdeiroAna);
		api.criarJogador("Bia", BIA, herdeiroBia);
		api.iniciarPartida();
	}

	// Ana passa a vez e Bia tambem: volta a vez da Ana
	private void passarRodada() {
		api.passarVez();
		api.passarVez();
	}
}
