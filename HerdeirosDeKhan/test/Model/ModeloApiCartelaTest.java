package Model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da 3a iteracao na ModeloApi: Singleton, Observer do cap. 17 e as
 * acoes ligadas a cartela (colunas, Khan e recursos virtuais).
 * Ana (vermelho) e Tolui; Bia (azul) e Jochi.
 */
public class ModeloApiCartelaTest {
	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ANA = "Vermelho";

	private ModeloApi api;
	private int idAna;

	@Before
	public void criaPartidaComDoisJogadores() {
		api = ModeloApi.getInstance();
		api.novaPartida();
		idAna = api.criarJogador("Ana", ANA, "Tolui");
		api.criarJogador("Bia", "Azul", "Jochi");
		api.iniciarPartida();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void getInstance_devolveSempreAMesmaInstancia() {
		assertSame("Singleton: uma unica ModeloApi", api, ModeloApi.getInstance());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void herdeiroSemCartela_partidaNaoInicia() {
		api.novaPartida();
		api.criarJogador("Ana", ANA, "Tolui");
		api.criarJogador("Bia", "Azul", "Khan");
		assertFalse("\"Khan\" nao e um herdeiro", api.iniciarPartida());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void ativarColunaVerdeDoTolui_daDoisMovimentos() {
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		assertEquals("Coluna verde do Tolui: 2 cavalos", 2, api.getMovimentosRestantes());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void segundaColunaNoMesmoTurno_recusada() {
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		assertFalse("So uma coluna por turno", api.ativarColuna(ModeloApi.COLUNA_VERMELHA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colunaComKhan_impedePassarAVezSemUsarOKhan() {
		api.ativarColuna(ModeloApi.COLUNA_AMARELA); // Tolui: cavalo, 2 punhos e Khan
		assertFalse("O Khan e obrigatorio nesta coluna", api.passarVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usarKhan_liberaPassarAVez() {
		api.ativarColuna(ModeloApi.COLUNA_AMARELA);
		api.usarKhan("Moeda Sarai");
		assertTrue("Depois de usar o Khan o turno pode acabar", api.passarVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void usarKhanSemIconeNaColuna_recusado() {
		api.ativarColuna(ModeloApi.COLUNA_VERDE); // sem Khan
		assertFalse("A coluna verde do Tolui nao tem o Khan", api.usarKhan("Moeda Sarai"));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void khanNaAreaDeMelhorias_saiDosDestinosPossiveis() {
		api.ativarColuna(ModeloApi.COLUNA_AMARELA);
		api.usarKhan(ModeloApi.AREA_DE_MELHORIAS);
		assertFalse("O Khan nao pode ficar onde ja esta",
				api.getDestinosDoKhan().contains(ModeloApi.AREA_DE_MELHORIAS));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void moedaVirtualDaColuna_pagaParteDaMelhoria() {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 1);
		api.ativarColuna(ModeloApi.COLUNA_VERMELHA); // Tolui: 1 moeda virtual
		api.comprarMelhoria("CAVALOS", ModeloApi.COLUNA_VERDE);
		assertEquals("1 moeda virtual + 1 real pagam a melhoria", 0,
				api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void colunaUsada_continuaOcupadaNaVezSeguinteDoJogador() {
		api.ativarColuna(ModeloApi.COLUNA_VERMELHA);
		api.passarVez();
		api.ativarColuna(ModeloApi.COLUNA_VERDE); // Bia
		api.passarVez();
		assertFalse("A coluna vermelha da Ana ainda tem a peca de ativacao",
				api.ativarColuna(ModeloApi.COLUNA_VERMELHA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void observador_pedeOEstadoPeloGet() {
		GuardaCorDaVez obs = new GuardaCorDaVez();
		api.add(obs);
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		assertEquals("No notify o observador consulta o observado (callback)", ANA, obs.cor);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void observadorRemovido_naoEAvisado() {
		GuardaCorDaVez obs = new GuardaCorDaVez();
		api.add(obs);
		api.remove(obs);
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		assertNull("Depois de remove() nao ha mais avisos", obs.cor);
	}

	// observador de teste: guarda a cor da vez que leu com get()
	private static class GuardaCorDaVez implements ObservadorIF {
		String cor = null;

		public void notify(ObservadoIF o) {
			cor = (String) o.get(ModeloApi.COR_DA_VEZ);
		}
	}
}
