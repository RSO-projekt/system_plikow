package rso;

import org.apache.thrift.transport.TTransportException;

/**
 * 
 * @author Daniel Pogrebniak
 *
 */
public class Client {

	public static void main(String[] args) {
		if (args.length == 0) {
		    System.err.println("Błąd: Brak parametrów wywołania!");
		    System.out.println(help());
		    System.exit(0);
		}
		FileSystem fs = new FileSystemImpl();
		try {
			fs.connect();
		} catch (TTransportException e) {
			System.err.println("Błąd: Nie udało się nawiązać połączenia z serwerem!");
			e.printStackTrace();
		}
		selectAction(args, fs);
		fs.disconnect();
	}

	private static void selectAction(String[] args, FileSystem fs) {
		try {
			switch (args[0]) {
			case "ls": case "-ls":
					fs.lookup(args[1]);
				break;
	
			default:
				System.err.println("Błąd: Niewłaściwe parametry wywołania!");
				break;
			}
		} catch (ConnectionLostException | EntryNotFoundException
				| InvalidOperationException e) {
			e.printStackTrace();
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
