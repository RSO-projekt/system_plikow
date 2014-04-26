package rso;

/**
 * 
 * @author Daniel Pogrebniak
 *
 */
public class Client {

	public static void main(String[] args) {
		if (args.length == 0) {
		    System.out.println("Błąd: Brak parametrów wywołania!");
		    System.out.println(help());
		    System.exit(0);
		}
	}

	static String help(){
		return "Dostępne parametry wywołania:\n"
				+ "-ls katalog \t\t Zwraca zawartość katalogu\n"
				+ "-mkdir katalog \t\t Tworzy nowy katalog\n"
				+ "-mkfile ścieżka rozmiar\t Tworzy nowy plik o podanym rozmiarze\n"
				+ "-rm ścieżka \t\t Usuwa podany plik lub katalog\n"
				+ "-mv obecna nowa \t Przenosi / Zmienia nazwę pliku/folderu\n"
				+ "-read ścieżka \t\t Odczyt ???\n" //TODO do ustalenia
				+ "-write ścieżka \t\t Zapis ???\n"; //TODO do ustalenia
	}
}
