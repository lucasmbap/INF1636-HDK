package View;

import java.io.File;

/**
 * Quem grava e le o estado da partida. A View so escolhe o arquivo (JFileChooser) e
 * entrega o File; o Controller consulta se pode salvar e coordena as operacoes do Model.
 * Cada metodo devolve null se deu certo, ou o motivo da falha (mostrado ao usuario).
 */
public interface OuvinteArquivos {

	boolean podeSalvarPartida();

	String salvarPartida(File arquivo);

	String recuperarPartida(File arquivo);
}
