package impl.server.master;

import impl.Configuration;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.TreeSet;

import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.EntryNotFound;
import rso.at.InvalidOperation;

public class ServerMain {

    private void start() throws EntryNotFound, InvalidOperation, TTransportException, SocketException {
        // Find all available IPs and save them in map.
        TreeSet<String> myIPs = new TreeSet<String>();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface ni : Collections.list(nets)) {
            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            for (InetAddress ia : Collections.list(addrs)) {
                if (ia instanceof Inet4Address) {
                    Inet4Address ia4 = (Inet4Address) ia;
                    myIPs.add(ia4.getHostAddress());
                }
            }
        }

        // Try to establish connection on first available port
        int myServerID = 0;
        TServerSocket masterServerTransport = null;
        TServerSocket clientServerTransport = null;
        TServerSocket dataServerTransport = null;
        FileSystemMonitor monitor = new FileSystemMonitor();
        for (String ip : Configuration.sMasterServerIPs) {
            if (myIPs.contains(ip)) {
                // Try to create socket on this port
                try {
                    masterServerTransport = new TServerSocket(Configuration.sMasterServerPorts.get(myServerID) +
                                                              Configuration.sMasterMasterOffset);
                    clientServerTransport = new TServerSocket(Configuration.sMasterServerPorts.get(myServerID) +
                                                              Configuration.sClientMasterOffset);
                    dataServerTransport = new TServerSocket(Configuration.sMasterServerPorts.get(myServerID) +
                                                            Configuration.sDataMasterOffset);
                    break;
                } catch (TTransportException e) {
                    monitor.log("Failed to establish server on family port nr: " + 
                                Configuration.sMasterServerPorts.get(myServerID));
                }
            }
            myServerID++;
        }

        // If not found local IP, abort.
        if (myServerID >= Configuration.sMasterServerIPs.size() ||
            masterServerTransport == null || 
            clientServerTransport == null ||
            dataServerTransport == null) {
            throw new InvalidOperation(101, "Cannot find local IP number in configuration file");
        }
        
        // Add other master servers
        for (int i = 0; i < Configuration.sMasterServerIPs.size(); ++i) {
            if (i != myServerID)
                monitor.addMasterConnection(i);
        }
        
        monitor.setServerID(myServerID);
        ClientMasterThread clientConn = new ClientMasterThread(clientServerTransport, monitor, myServerID);
        clientConn.start();
        
        DataMasterThread dataConn = new DataMasterThread(dataServerTransport, monitor, myServerID);
        dataConn.start();
        
        MasterMasterThread masterConn = new MasterMasterThread(masterServerTransport, monitor, myServerID);
        masterConn.start();
    }

    public static void main(String[] args) {
        ServerMain srv = new ServerMain();
        try {
            Configuration.load();
            srv.start();
        } catch (EntryNotFound e) {
            System.out.println("Error(" + e.code + "): " + e.message);
        } catch (InvalidOperation e) {
            System.out.println("Error(" + e.code + "): " + e.message);
        } catch (TTransportException e) {
            System.out.println("Error(TTransportException):");
            e.printStackTrace();
        } catch (SocketException e) {
            System.out.println("Error(Socket):");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error(IO): Configuration file not found or bad syntax");
        }
    }

}
