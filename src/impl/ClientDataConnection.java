package impl;

import rso.at.ClientDataService;
import rso.at.ClientDataService.Iface;

// Client - Data server connection.
public class ClientDataConnection extends Connection {
    public ClientDataConnection(int serverID) {
        super(Configuration.sDataServerIPs.get(serverID),
              Configuration.sDataServerPorts.get(serverID) + Configuration.sClientDataOffset, serverID, false);
        service = new ClientDataService.Client(protocol);
    }

    @Override
    public void reopen() {
        super.reopen();
        service = new ClientDataService.Client(protocol);
    }

    public Iface getService() {
        return service;
    }

    private ClientDataService.Iface service;
}