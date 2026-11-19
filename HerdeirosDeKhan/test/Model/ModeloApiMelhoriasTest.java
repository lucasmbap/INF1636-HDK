package Model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da area de melhorias pela ModeloApi (manual, pag. 9, 11 e 12).
 * Ana (vermelho) e Tolui; Bia (azul) e Chagatai. A area e sorteada, por isso
 * os testes leem as pecas pela API antes de agir.
 */
public class ModeloApiMelhoriasTest {
	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ANA = "Vermelho";

	private ModeloApi api;
	private int idAna;

	@Before
	public void criaPartidaComDoisJogadores() {
		api = ModeloApi.getInstance();
		api.novaPartida();
		idAna = api.criarJogador("Ana", ANA, "Tolui");
		api.criarJogador("Bia", "Azul", "Chagatai");
		api.iniciarPartida();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void inicioDaPartida_areaComCincoPecas() {
		for (int i = 0; i < ModeloApi.ESPACOS_DA_AREA; i++)
			assertNotNull("Espaco " + i + " revelado", api.getTipoMelhoriaNaArea(i));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void inicioDaPartida_pilhaCom39Pecas() {
		assertEquals("44 pecas - 5 reveladas", 39, api.getMelhoriasNaPilha());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarPeca_vaiParaAColunaDaCorDela() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 2);
		int cor = api.getCorMelhoriaNaArea(4);
		String tipo = api.getTipoMelhoriaNaArea(4);
		assertTrue("Compra na coluna da cor da peca", api.comprarMelhoria(4, cor));
		assertEquals("A peca esta na cartela", tipo, api.getMelhoriasDaColuna(ANA, cor).get(0));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarPeca_gastaDuasMoedas() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 3);
		api.comprarMelhoria(4);
		assertEquals("3 - 2 moedas", 1, api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarPeca_pecasDaAreaAndamParaDireita() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 2);
		String antesNoEspaco3 = api.getTipoMelhoriaNaArea(3);
		api.comprarMelhoria(4);
		assertEquals("A peca do espaco 3 andou para o 4", antesNoEspaco3, api.getTipoMelhoriaNaArea(4));
		assertNull("O espaco 0 ficou vazio", api.getTipoMelhoriaNaArea(0));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colunaDeOutraCor_recusada() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 2);
		int outra = (api.getCorMelhoriaNaArea(4) + 1) % 3;
		assertFalse("A peca tem de ir para a coluna da sua cor", api.comprarMelhoria(4, outra));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void semDuasMoedas_compraRecusada() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 1);
		assertFalse("Uma melhoria custa 2 moedas", api.comprarMelhoria(4));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colunaBranca_naoRecebeMelhoriaComprada() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 2);
		assertFalse("Compra so nas 3 colunas coloridas", api.comprarMelhoria(4, ModeloApi.COLUNA_BRANCA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void comprarDoTopo_tiraUmaDaPilha() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 2);
		api.comprarMelhoria(ModeloApi.TOPO_DA_PILHA);
		assertEquals("39 - 1", 38, api.getMelhoriasNaPilha());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanNaArea_preencheOsEspacosVazios() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 4);
		api.comprarMelhoria(4);
		api.comprarMelhoria(4);
		levarKhanParaArea();
		assertNotNull("Os espacos vazios recebem pecas da pilha", api.getTipoMelhoriaNaArea(0));
		assertEquals("2 pecas sairam da pilha", 37, api.getMelhoriasNaPilha());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanNaArea_iconeDeMoedaVazioDaUmaMoeda() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 4);
		api.comprarMelhoria(4);
		api.comprarMelhoria(4); // espacos 0 e 1 vazios
		levarKhanParaArea();
		assertEquals("Icone de moeda a mostra: 1 moeda", 1, api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanNaArea_iconeDeEspadaVazioDaUmaEspada() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 8);
		for (int i = 0; i < 4; i++)
			api.comprarMelhoria(4); // espacos 0 a 3 vazios
		levarKhanParaArea();
		assertEquals("Icone de espada a mostra: 1 espada", 1, api.getQuantidadeRecurso(idAna, TipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanNaAreaCheia_naoDaMoedaNemEspada() {
		levarKhanParaArea();
		assertEquals("Icones cobertos: nada de moeda", 0, api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
	}

	// auxiliar: Tolui ativa a coluna amarela (tem o Khan) e leva o Khan para a area
	private void levarKhanParaArea() {
		api.ativarColuna(ModeloApi.COLUNA_AMARELA);
		assertTrue("O Khan vai para a area de melhorias", api.usarKhan(ModeloApi.AREA_DE_MELHORIAS));
	}
}
