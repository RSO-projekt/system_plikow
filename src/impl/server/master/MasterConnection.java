package impl.server.master;

import rso.at.MasterMasterService;
import rso.at.MasterMasterService.Iface;

// Master server connection.
class MasterConnection extends Connection {
	public MasterConnection(String host, int port, int priority) {
		super(host, port, priority, "MasterMaster");
		service = new MasterMasterService.Client(protocol);
	}
	
	@Override
	public void reopen() {
		super.reopen();
		service = new MasterMasterService.Client(protocol);
	}

	public Iface getService() {
		return service;
	}
	private MasterMasterService.Iface service;
}