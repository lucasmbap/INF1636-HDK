package View;

import java.io.File;

/**
 * Quem grava e le o estado da partida. A View so escolhe o arquivo (JFileChooser) e
 * entrega o File; quem fala com o Model e o ouvinte (hoje, View.Main).
 * Cada metodo devolve null se deu certo, ou o motivo da falha (mostrado ao usuario).
 */
public interface OuvinteArquivos {

	String salvarPartida(File arquivo);

	String recuperarPartida(File arquivo);
}
