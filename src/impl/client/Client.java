package impl.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import rso.at.EntryNotFound;
import rso.at.FileEntry;
import rso.at.FileType;
import rso.at.InvalidOperation;

/**
 * 
 * @author Daniel Pogrebniak
 *
 */
public class Client {

	public static int main(String[] args) {
		if (args.length == 0) {
		    System.err.println("Błąd: Brak parametrów wywołania!");
		    System.out.println(help());
		    return 10;
		}
		Client client = new Client();
		FileSystem fs = null;
		try {
			fs = new FileSystemImpl();
		} catch (NumberFormatException e1) {
			System.err.println(e1.getMessage());
			return 11;
		} catch (FileNotFoundException e1) {
			System.err.println("Błąd: Nie znaleziono pliku konfiguracyjnego!");
			return 12;
		} catch (IOException e1) {
			System.err.println("Błąd: Nie udało się odczytać pliku konfiguracyjnego!");
			return 13;
		}
		try {
			fs.connect();
			int returnCode = client.selectAction(args, fs);
			if(returnCode !=0){
				return returnCode;
			}
		} catch (TTransportException e) {
			System.out.println("Błąd: Nie udało się nawiązać połączenia z żadnym serwerem!");
			return 14;
		} finally {
			fs.disconnect();
		}
		return 0;
	}

	private int selectAction(String[] args, FileSystem fs) {
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
			case "mkdir":
				fs.makeDirectory(args[1]);
				System.out.println("OK");
				break;
			case "mkfile":
				Integer size = new Integer(args[2]);
				fs.makeFile(args[1], size);
				System.out.println("OK");
				break;
			case "rm":
				fs.removeEntry(args[1]);
				System.out.println("OK");
				break;
			case "mv":
				fs.moveEntry(args[1], args[2]);
				System.out.println("OK");
				break;
			case "readPart":
				long offset = new Long(args[2]);
				long num = new Long(args[3]);
				byte[] bytes = fs.readFromFile(args[1], offset, num);
				System.out.println(bytes);
				break;
			case "readAll":
				readFile(fs, args[1], args[2]);
				System.out.println("OK");
				break;
			case "writePart":
				long offset2 = new Long(args[2]);
				fs.writeToFile(args[1], offset2, args[3].getBytes());
				System.out.println("OK");
				break;
			case "writeAll":
				writeFile(fs, args[1], args[2]);
				System.out.println("OK");
				break;
			case "help":
				System.out.println(help());
				break;
			default:
				System.err.println("Błąd: Niewłaściwe parametry wywołania!");
				return 16;
			}
		} catch (IOException e) {
			return 14;
		} catch (EntryNotFound e) {
			return 15;
		} catch (InvalidOperation e) {
			return 16;
		} catch (TException e) {
			return 17;
		}
		return 0;
	}

	private void showListOfEntries(List<FileEntry> entries){
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (entries.isEmpty()){
			System.out.println("Wybrany katalog jest pusty");
		} 
		for (FileEntry entry : entries){
			Date modificationTime = new Date((long)entry.getModificationTime()*1000);
			sb.append(format.format(modificationTime));
			if (entry.getType().equals(FileType.DIRECTORY))
				sb.append(" DIR  ");
			else
				sb.append(" FILE ");
			sb.append(entry.getSize());
			sb.append("\t");
			sb.append(entry.getName());
			sb.append("\n");
		}
		System.out.println(sb.toString());
	}
	
	private void readFile(FileSystem fs, String from, String to)
			throws  IOException, EntryNotFound, InvalidOperation, TException {
		FileEntry entry = fs.getFileEntry(from);
		long size = entry.getSize();
		byte[] bytes = fs.readFromFile(entry, 0, size);
		File file = new File(to);
		if (!file.exists())
			file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();
	}
	
	private void writeFile(FileSystem fs, String to, String from)
			throws IOException, EntryNotFound, InvalidOperation, TException {
		File file = new File(from);
		byte[] bytes = Files.readAllBytes(file.toPath());
		fs.writeToFile(to, 0, bytes);
	}
	
	private static String help(){
		return "Dostępne parametry wywołania:\n"
				+ "-ls katalog \t\t\t Zwraca zawartość katalogu\n"
				+ "-mkdir katalog \t\t\t Tworzy nowy katalog\n"
				+ "-mkfile ścieżka rozmiar\t\t Tworzy nowy plik o podanym rozmiarze\n"
				+ "-rm ścieżka \t\t\t Usuwa podany plik lub katalog\n"
				+ "-mv obecna nowa \t\t Przenosi / Zmienia nazwę pliku/folderu\n"
				+ "-readPart ścieżka offset rozmiar Odczyt fragmentu pliku z serwera\n"
				+ "-readAll zdalny lokalny \t Odczyt pliku z serwera\n"
				+ "-writePart ścieżka offset dane \t Modyfikacja pliku na serwerze\n" 
				+ "-writeAll zdalny lokalny \t Zapis pliku na serwerze\n"
				+ "-help \t\t\t\t Dostępne parametry wywołania"; 
	}
}
