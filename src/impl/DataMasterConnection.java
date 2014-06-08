package impl;

import rso.at.DataMasterService;
import rso.at.DataMasterService.Iface;

// Data - Master server connection.
public class DataMasterConnection extends Connection {
    public DataMasterConnection(int serverID) {
        super(Configuration.sMasterServerIPs.get(serverID),
              Configuration.sMasterServerPorts.get(serverID) + Configuration.sDataMasterOffset, serverID, true);
        service = new DataMasterService.Client(protocol);
    }

    @Override
    public void reopen() {
        super.reopen();
        service = new DataMasterService.Client(protocol);
    }

    public Iface getService() {
        return service;
    }

    private DataMasterService.Iface service;
}