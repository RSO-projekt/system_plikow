package rso;

import java.util.ArrayList;

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
		Client client = new Client();
		FileSystem fs = new FileSystemImpl();
		try {
			fs.connect();
			client.selectAction(args, fs);
		} catch (ConnectionLostException e) {
			System.err.println("Błąd: Nie udało się nawiązać połączenia z serwerem!");
			e.printStackTrace(); //TODO tylko do testów 
		} finally {
			fs.disconnect();
		}
	}

	private void selectAction(String[] args, FileSystem fs) {
		String action = null;
		if (args[0].startsWith("-"))
			action = args[0].substring(1);
		else
			action = args[0];
		try {
			switch (action) {
			case "ls":
				showListOfEntries(fs.lookup(args[1]));
				break;
			case "help":
				System.out.println(help());
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

	private void showListOfEntries(ArrayList<FileEntry> entries){
		StringBuilder sb = new StringBuilder();
		for (FileEntry entry : entries){
			sb.append(entry.getName());
			sb.append("\n");
		}
		System.out.println(sb.toString());
	}
	
	static String help(){
		return "Dostępne parametry wywołania:\n"
				+ "-ls katalog \t\t Zwraca zawartość katalogu\n"
				+ "-mkdir katalog \t\t Tworzy nowy katalog\n"
				+ "-mkfile ścieżka rozmiar\t Tworzy nowy plik o podanym rozmiarze\n"
				+ "-rm ścieżka \t\t Usuwa podany plik lub katalog\n"
				+ "-mv obecna nowa \t Przenosi / Zmienia nazwę pliku/folderu\n"
				+ "-read ścieżka \t\t Odczyt ???\n" //TODO do ustalenia
				+ "-write ścieżka \t\t Zapis ???\n" //TODO do ustalenia
				+ "-help \t\t\t Dostępne parametry wywołania"; 
	}
}
