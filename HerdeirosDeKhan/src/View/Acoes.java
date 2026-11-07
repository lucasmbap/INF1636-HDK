package View;

/**
 * Ponto unico onde o Controller se registra para receber as acoes feitas nas janelas
 * da View (hoje: escolha de coluna na cartela). Como as janelas sao criadas pela
 * View, o Controller faz apenas: Acoes.setOuvinte(this).
 */
public final class Acoes {
	private static OuvinteAcoes ouvinte = null;

	private Acoes() {
	}

	public static void setOuvinte(OuvinteAcoes novo) {
		ouvinte = novo;
	}

	/** Ouvinte registrado, ou null se o Controller ainda nao se registrou. */
	public static OuvinteAcoes getOuvinte() {
		return ouvinte;
	}
}
