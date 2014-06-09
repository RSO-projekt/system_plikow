package impl;

import rso.at.MasterMasterService;
import rso.at.MasterMasterService.Iface;

// Master - Master server connection.
public class MasterMasterConnection extends Connection {
    public MasterMasterConnection(int serverID) {
        super(Configuration.sMasterServerIPs.get(serverID),
              Configuration.sMasterServerPorts.get(serverID) + Configuration.sMasterMasterOffset, serverID);
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