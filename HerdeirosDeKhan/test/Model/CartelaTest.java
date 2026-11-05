package Model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da cartela do jogador: acoes impressas por herdeiro, pecas de
 * ativacao e melhorias (manual, paginas 6, 11, 12 e 16).
 */
public class CartelaTest {
	private static final int DEFAULT_TIMEOUT = 2000;

	private Cartela tolui;

	@Before
	public void criaCartelaDoTolui() {
		tolui = new Cartela("Tolui");
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void cartelaDoTolui_colunaVermelhaTemCavaloPunhoMoeda() {
		assertEquals("Acao impressa na coluna vermelha do Tolui", "[CAVALO, PUNHO, MOEDA]",
				tolui.getAcoesBase(ColunaCartela.VERMELHA).toString());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void nomeDoHerdeiro_ignoraMaiusculas() {
		assertEquals("\"jochi\" e o herdeiro Jochi", "Jochi", new Cartela("jochi").getHerdeiro());
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IllegalArgumentException.class)
	public void herdeiroDesconhecido_lancaExcecao() {
		new Cartela("Khan");
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColunaColorida_colunaFicaOcupada() {
		tolui.ativar(ColunaCartela.VERDE);
		assertTrue("A peca de ativacao fica na coluna verde", tolui.isOcupada(ColunaCartela.VERDE));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colunaOcupada_naoPodeSerAtivadaDeNovo() {
		tolui.ativar(ColunaCartela.VERDE);
		assertNull("A coluna verde ainda esta ocupada", tolui.ativar(ColunaCartela.VERDE));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColunaBranca_recuperaTodasAsPecas() {
		tolui.ativar(ColunaCartela.VERMELHA);
		tolui.ativar(ColunaCartela.VERDE);
		tolui.ativar(ColunaCartela.BRANCA);
		assertEquals("As 4 pecas de ativacao voltam", 4, tolui.getPecasLivres());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colunaBranca_temKhanEDoisCavalos() {
		AtivacaoColuna a = tolui.ativar(ColunaCartela.BRANCA);
		assertTrue("A coluna branca obriga a usar o Khan", a.temKhan() && a.getCavalos() == 2);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void melhoriaDeCavalos_somaDoisMovimentosNaColuna() {
		tolui.adicionarMelhoria(ColunaCartela.VERMELHA, TipoMelhoria.CAVALOS);
		tolui.fimDoTurno();
		assertEquals("1 cavalo impresso + 2 da melhoria", 3,
				tolui.ativar(ColunaCartela.VERMELHA).getCavalos());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void melhoriaColocadaNoTurno_naoValeAteOProximo() {
		tolui.adicionarMelhoria(ColunaCartela.VERMELHA, TipoMelhoria.CAVALOS);
		assertEquals("A melhoria comprada neste turno ainda nao vale", 1,
				tolui.ativar(ColunaCartela.VERMELHA).getCavalos());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void moedaImpressa_viraMoedaVirtual() {
		AtivacaoColuna a = tolui.ativar(ColunaCartela.VERMELHA);
		assertEquals("A coluna vermelha do Tolui da 1 moeda virtual", Integer.valueOf(1),
				a.getVirtuais().get(TipoTributo.MOEDA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colunaCheia_recusaQuintaMelhoria() {
		for (int i = 0; i < Cartela.ESPACOS_POR_COLUNA; i++)
			tolui.adicionarMelhoria(ColunaCartela.AMARELA, TipoMelhoria.PUNHO);
		assertFalse("Cada coluna tem 4 espacos",
				tolui.adicionarMelhoria(ColunaCartela.AMARELA, TipoMelhoria.PUNHO));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void linhaCompleta_daMelhoriaGratisNaBranca() {
		completaUmaLinha();
		assertEquals("Uma linha completa vale 1 melhoria gratis", 1, tolui.getMelhoriasGratisPendentes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void melhoriaGratis_soPodeSerColocadaUmaVezPorLinha() {
		completaUmaLinha();
		tolui.adicionarMelhoriaGratis(TipoMelhoria.MOEDA);
		assertFalse("A melhoria gratis da linha ja foi recebida",
				tolui.adicionarMelhoriaGratis(TipoMelhoria.MOEDA));
	}

	// auxiliar: 1 melhoria em cada coluna colorida
	private void completaUmaLinha() {
		tolui.adicionarMelhoria(ColunaCartela.VERMELHA, TipoMelhoria.ESPADA);
		tolui.adicionarMelhoria(ColunaCartela.VERDE, TipoMelhoria.YURT);
		tolui.adicionarMelhoria(ColunaCartela.AMARELA, TipoMelhoria.PUNHO);
	}
}
