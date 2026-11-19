package View;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Janelas de escolha de arquivo (JFileChooser) para salvar e recuperar partidas.
 * O usuario escolhe livremente a pasta e o nome; so o tipo (.txt) e pre-definido.
 */
final class SeletorDeArquivos {
	static final String EXTENSAO = "txt";

	private static File ultimaPasta = null;

	private SeletorDeArquivos() {
	}

	private static JFileChooser novo(String titulo) {
		JFileChooser seletor = new JFileChooser(ultimaPasta);
		seletor.setDialogTitle(titulo);
		seletor.setAcceptAllFileFilterUsed(false);
		seletor.setFileFilter(new FileNameExtensionFilter("Partida de Herdeiros de Khan (*.txt)", EXTENSAO));
		return seletor;
	}

	/** Devolve o arquivo escolhido para gravar (sempre terminado em .txt), ou null se cancelou. */
	static File escolherParaSalvar(Component pai) {
		JFileChooser seletor = novo("Salvar partida");
		while (true) {
			if (seletor.showSaveDialog(pai) != JFileChooser.APPROVE_OPTION)
				return null;
			File arquivo = comExtensao(seletor.getSelectedFile());
			if (arquivo.exists()) {
				int r = JOptionPane.showConfirmDialog(pai, "O arquivo " + arquivo.getName() + " ja existe. Substituir?",
						"Salvar partida", JOptionPane.YES_NO_OPTION);
				if (r != JOptionPane.YES_OPTION)
					continue; // volta ao seletor para escolher outro nome
			}
			ultimaPasta = arquivo.getParentFile();
			return arquivo;
		}
	}

	/** Devolve o arquivo escolhido para ler, ou null se cancelou. */
	static File escolherParaRecuperar(Component pai) {
		JFileChooser seletor = novo("Recuperar partida");
		if (seletor.showOpenDialog(pai) != JFileChooser.APPROVE_OPTION)
			return null;
		File arquivo = seletor.getSelectedFile();
		ultimaPasta = arquivo.getParentFile();
		return arquivo;
	}

	/** Acrescenta ".txt" se o usuario digitou um nome sem extensao. */
	static File comExtensao(File arquivo) {
		if (arquivo.getName().toLowerCase().endsWith("." + EXTENSAO))
			return arquivo;
		return new File(arquivo.getParentFile(), arquivo.getName() + "." + EXTENSAO);
	}

	/** Fluxo completo de "Salvar": escolhe o arquivo, grava e avisa do resultado. */
	static void salvar(Component pai, OuvinteArquivos ouvinte) {
		if (ouvinte == null)
			return;
		File arquivo = escolherParaSalvar(pai);
		if (arquivo == null)
			return;
		String erro = ouvinte.salvarPartida(arquivo);
		if (erro == null)
			JOptionPane.showMessageDialog(pai, "Partida salva em " + arquivo.getName() + ".", "Salvar partida",
					JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(pai, "Nao foi possivel salvar: " + erro, "Salvar partida",
					JOptionPane.ERROR_MESSAGE);
	}

	/** Fluxo completo de "Recuperar": escolhe o arquivo, le e avisa se falhou. */
	static void recuperar(Component pai, OuvinteArquivos ouvinte) {
		if (ouvinte == null)
			return;
		File arquivo = escolherParaRecuperar(pai);
		if (arquivo == null)
			return;
		String erro = ouvinte.recuperarPartida(arquivo);
		if (erro != null)
			JOptionPane.showMessageDialog(pai, "Nao foi possivel recuperar: " + erro, "Recuperar partida",
					JOptionPane.ERROR_MESSAGE);
	}
}
