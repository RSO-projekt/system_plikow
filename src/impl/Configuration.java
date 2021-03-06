package impl;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import rso.at.InvalidOperation;

public class Configuration {
    public static int sRedundancy = 3;
    public static int sChunkSize = 1000;
    public static ArrayList<String> sMasterServerIPs;
    public static ArrayList<Integer> sMasterServerPorts;
    public static ArrayList<String> sDataServerIPs;
    public static ArrayList<Integer> sDataServerPorts;
    public static int sReadTimeout = 5000;
    public static int sConnTimeout = 1000;
    public static String sConfigPath = "properties.conf";
    final public static int sClientMasterOffset = 0;
    final public static int sClientDataOffset = 1;
    final public static int sMasterMasterOffset = 2;
    final public static int sMasterDataOffset = 3;
    final public static int sDataMasterOffset = 4;
    final public static int sDataDataOffset = 5;

    static {
        sMasterServerIPs = new ArrayList<String>();
        sMasterServerPorts = new ArrayList<>();
        sDataServerIPs = new ArrayList<String>();
        sDataServerPorts = new ArrayList<>();
    }

    public static void load() throws IOException, InvalidOperation {
        Properties prop = new Properties();
        FileReader reader = new FileReader(sConfigPath);
        prop.load(reader);
        
        String redundancy = prop.getProperty("redundancy");
        if (redundancy == null) {
                throw new InvalidOperation(200, "\"redundancy\" (int) key expected in configuration file");
        }
        Configuration.sRedundancy = Integer.parseInt(redundancy);
        
        String chunkSize = prop.getProperty("chunk-size");
        if (chunkSize == null) {
                throw new InvalidOperation(201, "\"chunk-size\" (int) key expected in configuration file");
        }
        Configuration.sChunkSize = Integer.parseInt(chunkSize);
        
        String readTimeout = prop.getProperty("read-timeout");
        if (readTimeout == null) {
                throw new InvalidOperation(202, "\"read-timeout\" (int) key expected in configuration file");
        }
        Configuration.sReadTimeout = Integer.parseInt(readTimeout);
        
        String connTimeout = prop.getProperty("conn-timeout");
        if (connTimeout == null) {
                throw new InvalidOperation(202, "\"conn-timeout\" (int) key expected in configuration file");
        }
        Configuration.sConnTimeout = Integer.parseInt(connTimeout);
        
        String masterServerNum = prop.getProperty("master-server-num");
        if (masterServerNum == null) {
                throw new InvalidOperation(203, "\"master-server-num\" (int) key expected in configuration file");
        }
        int num = Integer.parseInt(masterServerNum);
        
        Configuration.sMasterServerIPs.clear();
        Configuration.sMasterServerPorts.clear();
        Configuration.sDataServerIPs.clear();
        Configuration.sDataServerPorts.clear();
        for(int i =0; i < num; i++){
                String key = "master-server" + String.valueOf(i) + "-ip";
                String mainServerIP = prop.getProperty(key);
                key = "master-server" + String.valueOf(i) + "-port";
                String mainServerPort = prop.getProperty(key);
                
                if (mainServerIP == null || mainServerPort == null) continue;
                
                Configuration.sMasterServerIPs.add(mainServerIP);
                Configuration.sMasterServerPorts.add(Integer.parseInt(mainServerPort));
        }
        
        num = Integer.parseInt(prop.getProperty("data-server-num"));
        for(int i =0; i < num; i++){
            String key = "data-server" + String.valueOf(i) + "-ip";
            String dataServerIP = prop.getProperty(key);
            key = "data-server" + String.valueOf(i) + "-port";
            String dataServerPort = prop.getProperty(key);
            
            if (dataServerIP == null || dataServerPort == null) continue;
            
            Configuration.sDataServerIPs.add(dataServerIP);
            Configuration.sDataServerPorts.add(Integer.parseInt(dataServerPort));
        }
    }
}
