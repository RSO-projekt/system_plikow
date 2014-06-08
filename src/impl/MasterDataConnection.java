package impl;

import rso.at.MasterDataService;
import rso.at.MasterDataService.Iface;

// Master - Data server connection.
public class MasterDataConnection extends Connection {
    public MasterDataConnection(int serverID) {
        super(Configuration.sDataServerIPs.get(serverID),
              Configuration.sDataServerPorts.get(serverID) + Configuration.sMasterDataOffset, serverID, true);
        service = new MasterDataService.Client(protocol);
    }

    @Override
    public void reopen() {
        super.reopen();
        service = new MasterDataService.Client(protocol);
    }

    public Iface getService() {
        return service;
    }

    private MasterDataService.Iface service;
}