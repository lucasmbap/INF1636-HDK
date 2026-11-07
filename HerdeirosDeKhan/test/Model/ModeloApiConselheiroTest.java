package Model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ModeloApiConselheiroTest {
	private ModeloApi api;
	private int idAna;

	@Before
	public void criarPartida() {
		api = ModeloApi.getInstance();
		api.novaPartida();
		idAna = api.criarJogador("Ana", "Vermelho", "Tolui");
		api.criarJogador("Bia", "Azul", "Jochi");
		api.iniciarPartida();
	}

	@Test
	public void entregaNoConselheiroConsomeTesourosEAplicaVotoEBonus() {
		adicionarSedas(2);
		api.definirMovimentos(20);
		assertTrue(api.moverPara("R2"));

		assertTrue(api.getConselheirosVisitadosNoTurno().contains("R2"));
		assertTrue(api.entregarTesourosConselheiro("R2", List.of("seda", "seda")));

		assertTrue(api.getNomesTesouros(idAna).isEmpty());
		assertEquals(1, api.getVotos(idAna));
		assertEquals(1, api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
	}

	@Test
	public void entregaNaoAceitaTesourosErradosNemGastaRecursos() {
		adicionarSedas(2);
		api.definirMovimentos(20);
		assertTrue(api.moverPara("R2"));

		assertFalse(api.entregarTesourosConselheiro("R2", List.of("ferro", "ferro")));

		assertEquals(List.of("seda", "seda"), api.getNomesTesouros(idAna));
		assertEquals(0, api.getVotos(idAna));
		assertEquals(0, api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
	}

	@Test
	public void bonusDoConselheiroSoEConcedidoNaPrimeiraEntrega() {
		adicionarSedas(4);
		api.definirMovimentos(20);
		assertTrue(api.moverPara("R2"));

		assertTrue(api.entregarTesourosConselheiro("R2", List.of("seda", "seda")));
		assertTrue(api.entregarTesourosConselheiro("R2", List.of("seda", "seda")));

		assertEquals(2, api.getVotos(idAna));
		assertEquals(1, api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
	}

	@Test
	public void conselheiroSoPodeReceberEntregaDepoisDeVisitado() {
		adicionarSedas(2);
		api.definirMovimentos(1);

		assertFalse(api.entregarTesourosConselheiro("R2", List.of("seda", "seda")));

		assertEquals(2, api.getNomesTesouros(idAna).size());
	}

	@Test
	public void karakorumUsaTresTesourosDaPecaSorteadaEConcedeDoisVotos() {
		List<String> requisitos = api.getTesourosExigidosEmKarakorum();
		for (String requisito : requisitos)
			api.adicionarTesouro(idAna, requisito);
		api.definirMovimentos(0);

		assertEquals(3, requisitos.size());
		assertEquals(requisitos, api.getRequisitosConselheiro("Karakorum"));
		assertTrue(api.entregarTesourosConselheiro("Karakorum", requisitos));

		assertEquals(2, api.getVotos(idAna));
		assertEquals(1, api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
		assertTrue(api.getNomesTesouros(idAna).isEmpty());
	}

	private void adicionarSedas(int quantidade) {
		for (int i = 0; i < quantidade; i++)
			api.adicionarTesouro(idAna, "seda");
	}
}
