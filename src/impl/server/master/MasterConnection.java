package impl.server.master;

import impl.Configuration;
import rso.at.MasterMasterService;
import rso.at.MasterMasterService.Iface;

// Master server connection.
class MasterConnection extends Connection {
    public MasterConnection(String host, int port, int serverID) {
        super(host, port + Configuration.sMasterMasterOffset, serverID);
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