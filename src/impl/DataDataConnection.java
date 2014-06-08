package impl;

import rso.at.DataDataService;
import rso.at.DataDataService.Iface;

// Data - Data server connection.
public class DataDataConnection extends Connection {
    public DataDataConnection(int serverID) {
        super(Configuration.sDataServerIPs.get(serverID),
              Configuration.sDataServerPorts.get(serverID) + Configuration.sDataDataOffset, serverID, true);
        service = new DataDataService.Client(protocol);
    }

    @Override
    public void reopen() {
        super.reopen();
        service = new DataDataService.Client(protocol);
    }

    public Iface getService() {
        return service;
    }

    private DataDataService.Iface service;
}