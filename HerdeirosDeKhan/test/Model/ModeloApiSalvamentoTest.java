package Model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da 4a iteracao: salvar e recuperar a partida pela ModeloApi.
 * O arquivo e .txt em ASCII puro; um arquivo invalido lanca IOException
 * e a partida so pode ser salva antes de o jogador da vez comecar a jogar.
 * Ana (vermelho) e Tolui; Bia (azul) e Jochi.
 */
public class ModeloApiSalvamentoTest {
	private static final int DEFAULT_TIMEOUT = 2000;
	private static final String ANA = "Vermelho";

	private ModeloApi api;
	private int idAna;
	private File arquivo;

	@Before
	public void criaPartidaComDoisJogadores() throws IOException {
		api = ModeloApi.getInstance();
		api.novaPartida();
		idAna = api.criarJogador("Ana", ANA, "Tolui");
		api.criarJogador("Bia", "Azul", "Jochi");
		api.iniciarPartida();
		arquivo = arquivoTemporario();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void antesDeAtivarColuna_podeSalvar() {
		assertTrue("No inicio da vez o botao de salvar fica habilitado", api.podeSalvar());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void depoisDeAtivarColuna_naoPodeSalvar() {
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		assertFalse("Depois que a jogada comecou nao se pode salvar", api.podeSalvar());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void semPartidaIniciada_naoPodeSalvar() {
		api.novaPartida();
		assertFalse("Sem partida em andamento nao ha o que salvar", api.podeSalvar());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void arquivoSalvo_temSoCaracteresAscii() throws IOException {
		api.novaPartida();
		api.criarJogador("Ana J\u00falia", ANA, "Tolui"); // nome com acento
		api.criarJogador("Bia", "Azul", "Jochi");
		api.iniciarPartida();
		api.salvar(arquivo);
		assertTrue("O arquivo de salvamento deve ser ASCII puro", soAscii(Files.readAllBytes(arquivo.toPath())));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void nomeComAcento_voltaIgualDepoisDeRecuperar() throws IOException {
		api.novaPartida();
		int id = api.criarJogador("Ana J\u00falia", ANA, "Tolui");
		api.criarJogador("Bia", "Azul", "Jochi");
		api.iniciarPartida();
		salvaZeraERecupera();
		assertEquals("O nome codificado em ASCII volta com o acento", "Ana J\u00falia", api.getNomeJogador(id));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void recursosDoJogador_voltamDepoisDeRecuperar() throws IOException {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 3);
		api.adicionarRecursos(idAna, TipoTributo.ESPADA, 1);
		salvaZeraERecupera();
		assertEquals("Moedas da Ana", 3, api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
		assertEquals("Espadas da Ana", 1, api.getQuantidadeRecurso(idAna, TipoTributo.ESPADA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void tesourosDoJogador_voltamDepoisDeRecuperar() throws IOException {
		api.adicionarTesouro(idAna, "seda");
		api.adicionarTesouro(idAna, "ferro");
		salvaZeraERecupera();
		assertEquals("Tesouros da Ana na mesma ordem", "[seda, ferro]", api.getNomesTesouros(idAna).toString());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void corDaVez_voltaDepoisDeRecuperar() throws IOException {
		api.passarVez(); // vez da Bia
		String cor = api.getCorDaVez();
		salvaZeraERecupera();
		assertEquals("A vez continua com o mesmo jogador", cor, api.getCorDaVez());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void tabuleiro_voltaIgualDepoisDeRecuperar() throws IOException {
		api.definirMovimentos(20);
		api.moverPara("R2");
		api.passarVez();
		String posicaoAna = api.getPosicao(ANA);
		String khan = api.getPosicaoKhan();
		String cartas = api.getCartasAbertasNaMesa().toString();
		salvaZeraERecupera();
		assertEquals("Peao da Ana no mesmo lugar", posicaoAna, api.getPosicao(ANA));
		assertEquals("Khan no mesmo lugar", khan, api.getPosicaoKhan());
		assertEquals("Mesmas cartas abertas na mesa (foram sorteadas)", cartas,
				api.getCartasAbertasNaMesa().toString());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void salvarDeNovoAposRecuperar_geraArquivoIdentico() throws IOException {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 2);
		api.adicionarTesouro(idAna, "seda");
		api.passarVez();
		api.salvar(arquivo);
		api.novaPartida();
		api.recuperar(arquivo);
		File segundo = arquivoTemporario();
		api.salvar(segundo);
		assertEquals("Nada se perde ao recuperar e salvar de novo", lerTexto(arquivo), lerTexto(segundo));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void recuperar_avisaOsObservadores() throws IOException {
		api.salvar(arquivo);
		ContaAvisos obs = new ContaAvisos();
		api.add(obs);
		api.recuperar(arquivo);
		assertTrue("A View precisa redesenhar depois de recuperar", obs.avisos > 0);
		api.remove(obs);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IOException.class)
	public void arquivoQueNaoEPartida_lancaIOException() throws IOException {
		escreve("lista de compras\nleite\npao\n");
		api.recuperar(arquivo);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IOException.class)
	public void arquivoSemLinhaFim_lancaIOException() throws IOException {
		api.salvar(arquivo);
		String texto = lerTexto(arquivo);
		escreve(texto.substring(0, texto.lastIndexOf("FIM")));
		api.recuperar(arquivo);
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IOException.class)
	public void arquivoInexistente_lancaIOException() throws IOException {
		arquivo.delete();
		api.recuperar(arquivo);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void arquivoInvalido_naoMudaAPartidaAtual() throws IOException {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 4);
		escreve("HERDEIROS_DE_KHAN|1\nLIXO|x\nFIM\n");
		try {
			api.recuperar(arquivo);
			fail("Um arquivo invalido deveria lancar IOException");
		} catch (IOException e) {
			assertEquals("A partida em andamento continua a mesma", 4,
					api.getQuantidadeRecurso(idAna, TipoTributo.MOEDA));
		}
	}

	@Test(timeout = DEFAULT_TIMEOUT, expected = IOException.class)
	public void salvarSemArquivo_lancaIOException() throws IOException {
		api.salvar(null);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void areaDeMelhorias_voltaIgualDepoisDeRecuperar() throws IOException {
		api.adicionarRecursos(idAna, TipoTributo.MOEDA, 2);
		api.comprarMelhoria(4); // deixa um espaco vazio
		String antes = descreverArea();
		salvaZeraERecupera();
		assertEquals("Mesmas pecas, cores e espacos vazios na area", antes, descreverArea());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void pilhaDeMelhorias_voltaComOMesmoTamanho() throws IOException {
		int antes = api.getMelhoriasNaPilha();
		salvaZeraERecupera();
		assertEquals("Pecas que restam na pilha", antes, api.getMelhoriasNaPilha());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void yurtDoToluiEmKarakorum_naoDuplicaAoRecuperar() throws IOException {
		salvaZeraERecupera(); // Ana e Tolui
		assertEquals("Um yurt so em Karakorum", 1, api.getYurtsNaParada("Karakorum").size());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void segundoPeaoDaAltani_voltaNoMesmoLugar() throws IOException {
		api.novaPartida();
		api.criarJogador("Ana", ANA, "Altani");
		api.criarJogador("Bia", "Azul", "Jochi");
		api.iniciarPartida();
		api.escolherPeao(2);
		api.ativarColuna(ModeloApi.COLUNA_VERDE);
		api.moverPara("R9");
		api.passarVez();
		salvaZeraERecupera();
		assertEquals("2o peao em R9", "R9", api.getPosicaoSegundoPeao(ANA));
		assertEquals("A Altani continua com o 2o peao escolhido", 2, api.getPeaoEscolhido(ANA));
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void melhoriasIniciaisDoJochi_voltamDepoisDeRecuperar() throws IOException {
		api.novaPartida();
		api.criarJogador("Ana", ANA, "Jochi");
		api.criarJogador("Bia", "Azul", "Tolui");
		api.iniciarPartida();
		String antes = api.getMelhoriasIniciaisJochi(ANA).toString();
		salvaZeraERecupera();
		assertEquals("As 2 melhorias que o Jochi ainda vai escolher", antes,
				api.getMelhoriasIniciaisJochi(ANA).toString());
	}

	// auxiliares

	private String descreverArea() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ModeloApi.ESPACOS_DA_AREA; i++)
			sb.append(api.getTipoMelhoriaNaArea(i)).append('/').append(api.getCorMelhoriaNaArea(i)).append(' ');
		return sb.toString();
	}

	private void salvaZeraERecupera() throws IOException {
		api.salvar(arquivo);
		api.novaPartida(); // apaga tudo antes de ler o arquivo
		api.recuperar(arquivo);
	}

	private static File arquivoTemporario() throws IOException {
		File f = File.createTempFile("partida", ".txt");
		f.deleteOnExit();
		return f;
	}

	private void escreve(String texto) throws IOException {
		Files.write(arquivo.toPath(), texto.getBytes(StandardCharsets.US_ASCII));
	}

	private static String lerTexto(File f) throws IOException {
		return new String(Files.readAllBytes(f.toPath()), StandardCharsets.US_ASCII);
	}

	private static boolean soAscii(byte[] bytes) {
		for (byte b : bytes)
			if (b < 0) // em Java, bytes acima de 127 sao negativos
				return false;
		return true;
	}

	// observador de teste: conta quantas vezes foi avisado
	private static class ContaAvisos implements ObservadorIF {
		int avisos = 0;

		public void notify(ObservadoIF o) {
			avisos++;
		}
	}
}
