package impl;

import rso.at.ClientMasterService;
import rso.at.ClientMasterService.Iface;

// Client - Master server connection.
public class ClientMasterConnection extends Connection {
    public ClientMasterConnection(int serverID) {
        super(Configuration.sMasterServerIPs.get(serverID),
              Configuration.sMasterServerPorts.get(serverID) + Configuration.sClientMasterOffset, serverID, false);
        service = new ClientMasterService.Client(protocol);
    }

    @Override
    public void reopen() {
        super.reopen();
        service = new ClientMasterService.Client(protocol);
    }

    public Iface getService() {
        return service;
    }

    private ClientMasterService.Iface service;
}