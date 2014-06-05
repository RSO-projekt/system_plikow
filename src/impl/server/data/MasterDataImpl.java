package impl.server.data;
import org.apache.thrift.TException;

import rso.at.*;

//Tylko master
public class MasterDataImpl implements MasterDataService.Iface{

	@Override
	public void allocateFile(long fileID, long newFileSize)
			throws InvalidOperation, TException {
		//TODO stworzyc klase ze struktora pliku (id, rozmiar dane)
		//TODO storzyc klase "zmiana" - do zatwierdzenia przez MasterServer
	}

	@Override
	public void createFileTransaction(Transaction transaction, long fileID,
			long offset, long size) throws InvalidOperation, TException {
		// TODO Auto-generated method stub
		//master wysle Transaction jako informacje ze klient ma zamiar wyslac zmiane (odczyt) na fileID
		
	}

	@Override
	public void applyChanges(long fileID) throws InvalidOperation, TException {
		// TODO Auto-generated method stub
		// mozna zalozyc łate na plik ("zmiana")
	}

}
