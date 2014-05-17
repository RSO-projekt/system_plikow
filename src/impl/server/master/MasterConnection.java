package impl.server.master;

import java.io.IOException;

import rso.at.MasterMasterService;
import rso.at.MasterMasterService.Iface;

// Master server connection.
class MasterConnection extends Connection {
	public MasterConnection(String host, int port, int priority) throws IOException {
		super(host, port, priority, "MasterMaster");
		service = new MasterMasterService.Client(protocol);
	}
	
	@Override
	public void reopen() {
		try {
			super.reopen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		service = new MasterMasterService.Client(protocol);
	}

	public Iface getService() {
		return service;
	}
	private MasterMasterService.Iface service;
}