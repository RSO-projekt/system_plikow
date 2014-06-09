package impl.server.data;

import impl.Configuration;
import impl.DataDataConnection;
import impl.DataMasterConnection;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.sun.corba.se.impl.ior.ByteBuffer;

import rso.at.EntryNotFound;
import rso.at.FileEntryExtended;
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
        ClientDataThread clientDataConn = new ClientDataThread(clientServerTransport, myServerID);     
        DataDataThread dataDataConn = new DataDataThread(dataServerTransport, myServerID);
        
        System.out.println("Getting files from other Data Servers");
        DataMasterConnection conn = null;
        for(int i =0 ; i < Configuration.sMasterServerIPs.size(); ++i){
            conn = new DataMasterConnection(i);
            if(conn.wasCreated()){
                break;
            }
        }
        if(!conn.wasCreated()){
            System.out.println("No master server is active");
            return;
        }
        List<FileEntryExtended> entries;
        try {
            entries =  conn.getService().getMirroredFileList(myServerID);
        } catch (TException e) {
            System.out.println("Connection to master server was aborted");
            return;
        }
        for(FileEntryExtended entry: entries){
            DataDataConnection dataConn = null;
            for(Integer mirror: entry.mirrors){
                if(mirror == myServerID)continue;
                
                dataConn = new DataDataConnection(mirror);
                if(dataConn.wasCreated()){
                    try {
                        java.nio.ByteBuffer file = dataConn.getService().getFile(entry);
                        FileData.getInstance().createFile(entry, file.array());
                        System.out.println("File downloaded fileID : " + entry.entry.id);
                        break;
                    } catch (TException e) {
                       
                    }
                }
            }
        }
        
        masterDataConn.start();
        clientDataConn.start();
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