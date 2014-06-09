package impl.server.data;

import impl.Configuration;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.TreeSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import rso.at.EntryNotFound;
import rso.at.InvalidOperation;

public class ServerDataMain {
    
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
        for (String ip : Configuration.sDataServerIPs) {
            if (myIPs.contains(ip)) {
                // Try to create socket on this port
                try {
                    masterServerTransport = new TServerSocket(Configuration.sDataServerPorts.get(myServerID) +
                                                              Configuration.sMasterDataOffset);
                    clientServerTransport = new TServerSocket(Configuration.sDataServerPorts.get(myServerID) +
                                                              Configuration.sClientDataOffset);
                    dataServerTransport   = new TServerSocket(Configuration.sDataServerPorts.get(myServerID) +
                                                              Configuration.sDataDataOffset);
                    break;
                } catch (TTransportException e) {
                    System.out.println("Failed to establish server on family port nr: " + 
                                       Configuration.sDataServerPorts.get(myServerID));
                }
            }
            myServerID++;
        }

        // If not found local IP, abort.
        if (myServerID >= Configuration.sDataServerIPs.size() ||
            masterServerTransport == null || 
            clientServerTransport == null ||
            dataServerTransport == null) {
            throw new InvalidOperation(101, "Cannot find local IP number in configuration file");
        }

        System.out.println("Starting data server on " + 
                            Configuration.sDataServerIPs.get(myServerID) + ":" +
                            Configuration.sDataServerPorts.get(myServerID));
        
        MasterDataThread masterDataConn = new MasterDataThread(masterServerTransport, myServerID);
        masterDataConn.start();

        ClientDataThread clientDataConn = new ClientDataThread(clientServerTransport, myServerID);
        clientDataConn.start();
        
        DataDataThread dataDataConn = new DataDataThread(dataServerTransport, myServerID);
        dataDataConn.start();
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        ServerDataMain srv = new ServerDataMain();
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