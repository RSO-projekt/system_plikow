package impl.server.master;

import impl.Configuration;
import impl.MasterDataConnection;
import impl.MasterMasterConnection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.management.monitor.Monitor;

import org.apache.thrift.TException;

import rso.at.FileEntry;
import rso.at.FileEntryExtended;
import rso.at.EntryNotFound;
import rso.at.FileState;
import rso.at.FileSystemSnapshot;
import rso.at.FileType;
import rso.at.HostNotPermitted;
import rso.at.InvalidOperation;
import rso.at.Transaction;
import rso.at.TransactionType;

public class FileSystemMonitor {
    // Map containing all IDs of file system's entries.
    private TreeMap<Long, FileEntryExtended> idMap;
    // Map containing all children of parent with specified ID.
    private TreeMap<Long, TreeSet<FileEntryExtended>> parentIdMap;
    // Map containing information about current transaction
    private TreeMap<Long, List<Transaction>> fileTransactions;
    
    // Next available ID.
    private Long nextId;
    // Version of a file system. Every change increment this value by 1.
    private Long fsVersion;

    // Internal strings for getParentPath() function.
    private String pathParent;
    private String pathName;
    
    // Random number generator
    private Random rnd = new Random();

    // Current server ID
    int serverID;

    // Current coordinator server ID
    int coordServerID;

    // Transaction token
    private int transactionToken;

    public void setServerID(int serverID) {
        this.serverID = this.coordServerID = serverID;
    }

    enum Mode {
        MASTER, SLAVE
    }

    // Current mode of a master
    Mode mode;

    // List of all redundant master servers
    private ArrayList<MasterMasterConnection> masterList;

    // Function checks privileges of a server
    private synchronized void checkPriviliges(boolean external) throws HostNotPermitted {
        if (external && mode == Mode.SLAVE) {
            startElection();
        }
    }

    // General logging function
    public synchronized void log(String message) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        System.out.println("[" + df.format(new Date()) + "] " + message);
    }

    // Display short info about an entry
    private String showFileEntryExtended(FileEntryExtended entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("[" + (entry.entry.type == FileType.DIRECTORY ? "DIR: \"" : "FILE: \"") + entry.entry.name + "\", ");
        sb.append("ID: " + entry.entry.id + ", ");
        sb.append("pID: " + entry.entry.parentID + ", ");
        sb.append("size: " + entry.entry.size + ", ");
        sb.append("ver: " + entry.entry.version + ", ");
        sb.append("on:");
        for (int mirror : entry.mirrors) {
            sb.append(" " + mirror);
        }
        sb.append("]");
        return sb.toString();
    }

    // Add master connection to the list
    public synchronized void addMasterConnection(int serverID) {
        MasterMasterConnection conn = new MasterMasterConnection(serverID);
        masterList.add(conn);
    }

    // Private function to easily create new File Entry Extended object
    private FileEntryExtended createFileEntryExtended(FileType fileType, long time, long parentId, long size, String name) {
        FileEntry fe = new FileEntry(fileType, time, nextId, parentId, 0, size, name);
        ++nextId;
        
        // Select random mirrors
        ArrayList<Integer> mirrors = new ArrayList<Integer>();
        int start = rnd.nextInt(Configuration.sDataServerIPs.size());
        for (int i = start; i < start + Configuration.sRedundancy; ++i) {
            int newMirror = i % Configuration.sDataServerIPs.size();
            if (!mirrors.contains(newMirror))
                mirrors.add(newMirror);
        }
        
        return new FileEntryExtended(fe, mirrors, FileState.IDLE);
    }

    // Check if new entry's name is correct, it's parent exists and it doesn't
    // have child with the same name.
    private void checkParentAndName(FileEntry parent, String name) throws InvalidOperation {
        if (name.contains("/")) {
            throw new InvalidOperation(9, "You cannot put '/' in directory name");
        }
        if (parent.type == FileType.FILE) {
            throw new InvalidOperation(10, "Invalid path. You cannot create folder in a file");
        }

        TreeSet<FileEntryExtended> childrenSet = parentIdMap.get(parent.id);
        if (childrenSet == null) {
            throw new InvalidOperation(19, "Parent doesn't exist: " + parent.name);
        }

        boolean foundName = false;
        for (FileEntryExtended fileEntryExtended : childrenSet) {
            if (fileEntryExtended.entry.name.equals(name)) {
                foundName = true;
                break;
            }
        }

        if (foundName) {
            throw new InvalidOperation(20, "File with specified name already exists");
        }
    }

    // Splits path into two elements save to monitor's private fields:
    // full path to a parent and a name of a new child.
    private void getParentPath(String path) throws EntryNotFound {
        if (path.length() <= 1) {
            throw new EntryNotFound(7, "Empty path not expected");
        }
        if (path.charAt(0) != '/') {
            throw new EntryNotFound(8, "Path should start from root '/'");
        }
        if (path.charAt(path.length() - 1) == '/')
            path = path.substring(0, path.length() - 1);

        pathParent = path;
        int lastSlash = path.lastIndexOf('/');
        pathParent = path.substring(0, lastSlash);
        if (pathParent.isEmpty())
            pathParent = "/";

        pathName = path.substring(lastSlash + 1, path.length());
    }

    // Default constructor creates root folder.
    public FileSystemMonitor() {
        this.serverID = coordServerID = 0;
        mode = Mode.SLAVE;

        nextId = new Long(0);
        fsVersion = new Long(0);

        masterList = new ArrayList<MasterMasterConnection>();
        fileTransactions = new TreeMap<Long, List<Transaction>>();
        
        idMap = new TreeMap<Long, FileEntryExtended>();
        parentIdMap = new TreeMap<Long, TreeSet<FileEntryExtended>>();
        FileEntryExtended root = createFileEntryExtended(FileType.DIRECTORY, System.currentTimeMillis() / 1000, 0, 0, "root");
        idMap.put(0l, root);
        TreeSet<FileEntryExtended> rootSet = new TreeSet<>();
        parentIdMap.put(0l, rootSet);
        transactionToken = 0;
    }

    // Return file's entry based on it's path.
    public synchronized FileEntryExtended getEntry(boolean external, String path) throws EntryNotFound, HostNotPermitted {
        checkPriviliges(external);

        if (path.isEmpty()) {
            throw new EntryNotFound(0, "Empty path not expected");
        }
        String[] pathArray = path.split("/");
        if (pathArray.length != 0 && !pathArray[0].isEmpty()) {
            throw new EntryNotFound(1, "Path should start from root '/'");
        }
        FileEntryExtended fe = idMap.get(0l);
        FileEntryExtended fe2 = fe;

        for (int i = 1; i < pathArray.length; ++i) {
            TreeSet<FileEntryExtended> dir = parentIdMap.get(fe.entry.id);
            if (dir == null) {
                throw new EntryNotFound(2, "Cannot find directory's children: " + path);
            }
            for (FileEntryExtended fileEntryExtended : dir) {
                if (fileEntryExtended.entry.name.equals(pathArray[i]))
                    fe2 = fileEntryExtended;
            }
            if (fe == fe2)
                throw new EntryNotFound(3, "Cannot find drectory or file: " + path);
            fe = fe2;
        }
        return fe.deepCopy();
    }

    // List all children of a parent
    public synchronized List<FileEntry> lookup(boolean external, String path, FileEntry parent) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);

        FileEntryExtended entry = null;
        if (!path.isEmpty())
            entry = getEntry(false, path);
        else if (parent != null)
            entry = idMap.get(parent.id);
        if (entry == null)
            throw new EntryNotFound(4, "Directory doesn't exist: " + parent.name);
        if (entry.entry.type == FileType.FILE)
            throw new InvalidOperation(5, "File cannot has children: " + entry.entry.name);

        TreeSet<FileEntryExtended> children = parentIdMap.get(entry.entry.id);
        if (children == null)
            throw new EntryNotFound(6, "[CRITICAL] Directory doesn't exist: " + entry.entry.name);

        List<FileEntry> childrenList = new ArrayList<FileEntry>();
        for (FileEntryExtended fileEntry : children) {
            childrenList.add(fileEntry.entry.deepCopy());
        }
        return childrenList;
    }

    // Make new directory based on it's new path
    public synchronized FileEntry makeDirectory(boolean external, String path) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);
        getParentPath(path);
        FileEntryExtended parentDir = getEntry(false, pathParent);
        return makeDirectory2(false, parentDir.entry, pathName);
    }

    // Make new directory based on parent's descriptor
    public synchronized FileEntry makeDirectory2(boolean external, FileEntry parent, String name) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);
        checkParentAndName(parent, name);
        FileEntryExtended dir = createFileEntryExtended(FileType.DIRECTORY, System.currentTimeMillis() / 1000, parent.id, 0, name);
        idMap.put(dir.entry.id, dir);
        parentIdMap.put(dir.entry.id, new TreeSet<FileEntryExtended>());
        parentIdMap.get(parent.id).add(dir);

        broadcastCreateEntry(dir);
        return dir.entry.deepCopy();
    }

    // Send information about new entry to other, redundant servers.
    public synchronized void broadcastCreateEntry(FileEntryExtended entry) {
        if (mode == Mode.SLAVE)
            return;
        String msg = "New entry: " + showFileEntryExtended(entry);
        log(msg);

        fsVersion++;
        for (MasterMasterConnection conn : masterList) {
            int retries = 0;
            while (retries < 2) {
                try {
                    conn.getService().updateCreateEntry(serverID, fsVersion, entry);
                    log("Broadcasted to " + conn.getHostAddress() + ":" + conn.getHostPort());
                    break;
                } catch (TException e) {
                    conn.reopen();
                    retries++;
                }
            }
            if (retries == 2) {
                log("Can't broadcast new entry to " + conn.getHostAddress() + ":" + conn.getHostPort());
            }
        }
    }

    // Create new file in the server.
    public synchronized FileEntry makeFile(boolean external, String path) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);
        getParentPath(path);
        FileEntryExtended parentEntry = getEntry(false, pathParent);
        return makeFile2(false, parentEntry.entry, pathName);
    }

    // Create new file in the server by using descriptor.
    public synchronized FileEntry makeFile2(boolean external, FileEntry parent, String name) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);
        checkParentAndName(parent, name);
        FileEntryExtended file = createFileEntryExtended(FileType.FILE, System.currentTimeMillis() / 1000, parent.id, 0, name);

        idMap.put(file.entry.id, file);
        parentIdMap.get(parent.id).add(file);
        broadcastCreateEntry(file);
        return file.entry.deepCopy();
    }

    public synchronized FileEntry allocateFile(boolean external, String path, long size) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);

        return null;
    }

    // Create new file in the server by using descriptor.
    public synchronized FileEntry allocateFile2(boolean external, FileEntry entry, long size) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);
        return null;
    }

    // Remove entry from file system
    public synchronized void removeEntry(boolean external, String path) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);
        FileEntryExtended removingEntry = getEntry(false, path);
        removeEntry2(false, removingEntry.entry);
    }

    // Remove entry from file system by using descriptor
    public synchronized void removeEntry2(boolean external, FileEntry entry) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);
        FileEntryExtended removingEntry = idMap.get(entry.id);
        if (removingEntry == null) {
            throw new EntryNotFound(13, "Entry for removal not found: " + entry.name);
        }
        if (entry.id == 0) {
            throw new InvalidOperation(30, "Cannot remove root directory");
        }
        if (removingEntry.entry.type == FileType.DIRECTORY) {
            TreeSet<FileEntryExtended> childrenSet = parentIdMap.get(removingEntry.entry.id);
            if (!childrenSet.isEmpty()) {
                throw new InvalidOperation(14, "You cannot remove unempty directory: " + entry.name);
            }
            parentIdMap.remove(removingEntry.entry.id);
        }

        TreeSet<FileEntryExtended> childrenSet2 = parentIdMap.get(removingEntry.entry.parentID);
        if (childrenSet2 == null) {
            throw new InvalidOperation(15, "[CRITICAL] Entry doesn't have parent: " + entry.name);
        }

        idMap.remove(removingEntry.entry.id);
        childrenSet2.remove(removingEntry);
        broadcastRemoveEntry(removingEntry);
    }

    // Broadcast removal of an entry to other servers
    public synchronized void broadcastRemoveEntry(FileEntryExtended entry) {
        if (mode == Mode.SLAVE)
            return;
        String msg = "Removed entry: " + showFileEntryExtended(entry);
        log(msg);

        fsVersion++;
        for (MasterMasterConnection conn : masterList) {
            int retries = 0;
            while (retries < 2) {
                try {
                    conn.getService().updateRemoveEntry(serverID, fsVersion, entry);
                    log("Broadcasted to " + conn.getHostAddress() + ":" + conn.getHostPort());
                    break;
                } catch (TException e) {
                    conn.reopen();
                    retries++;
                }
            }
            if (retries == 2) {
                log("Can't broadcast removed entry to " + conn.getHostAddress() + ":" + conn.getHostPort());
            }
        }
    }

    // Move entry in file system
    public synchronized FileEntry moveEntry(boolean external, String fromPath, String toPath) throws EntryNotFound, InvalidOperation, HostNotPermitted {
        checkPriviliges(external);
        getParentPath(toPath);
        FileEntryExtended parent = getEntry(false, pathParent);
        FileEntryExtended entry = getEntry(false, fromPath);
        return moveEntry2(false, entry.entry, parent.entry, pathName);
    }

    // Move entry in file system by using descriptor
    public synchronized FileEntry moveEntry2(boolean external, FileEntry entry, FileEntry parent, String name) throws EntryNotFound, InvalidOperation,
            HostNotPermitted {
        checkPriviliges(external);
        checkParentAndName(parent, name);
        FileEntryExtended entryExtended = idMap.get(entry.id);

        if (entryExtended == null) {
            throw new InvalidOperation(16, "Entry doesn't exist: " + entry.name);
        }

        FileEntryExtended oldEntryExtended = entryExtended.deepCopy();

        if (entry.parentID == parent.id) {
            entryExtended.entry.name = name;
            broadcastMoveEntry(oldEntryExtended, entryExtended);
            return entryExtended.entry.deepCopy();
        }

        TreeSet<FileEntryExtended> oldParentChildrenSet = parentIdMap.get(entry.parentID);
        if (oldParentChildrenSet == null) {
            throw new InvalidOperation(17, "Entry doesn't have parent: " + entry.name);
        }
        oldParentChildrenSet.remove(entryExtended);

        TreeSet<FileEntryExtended> newParentChildrenSet = parentIdMap.get(parent.id);
        if (newParentChildrenSet == null) {
            throw new InvalidOperation(18, "New parent doesn't exist: " + entry.name);
        }
        newParentChildrenSet.add(entryExtended);
        entryExtended.entry.parentID = parent.id;
        entryExtended.entry.name = name;

        broadcastMoveEntry(oldEntryExtended, entryExtended);
        return entryExtended.entry.deepCopy();
    }

    // Broadcast all moved entries to other servers.
    public synchronized void broadcastMoveEntry(FileEntryExtended oldEntry, FileEntryExtended newEntry) {
        if (mode == Mode.SLAVE)
            return;
        String msg = "Moved entry: " + showFileEntryExtended(oldEntry) + " -> " + showFileEntryExtended(newEntry);
        log(msg);

        fsVersion++;
        for (MasterMasterConnection conn : masterList) {
            int retries = 0;
            while (retries < 2) {
                try {
                    conn.getService().updateMoveEntry(serverID, fsVersion, oldEntry, newEntry);
                    log("Broadcasted to " + conn.getHostAddress() + ":" + conn.getHostPort());
                    break;
                } catch (TException e) {
                    conn.reopen();
                    retries++;
                }
            }
            if (retries == 2) {
                log("Can't broadcast moved entry to " + conn.getHostAddress() + ":" + conn.getHostPort());
            }
        }
    }

    public synchronized void updateEntry(int serverID, long fsVersion, FileEntryExtended entry) {
        // If got message from lower priority server, start election.
        if (serverID > this.serverID) {
            startElection();
            return;
        }
        
        // If file system version is incorrect, we need to download whole
        // snapshot
        if (fsVersion != this.fsVersion + 1) {
            recreateFileSystem(coordServerID);
            return;
        }
        
        // Replace a file
        idMap.put(entry.entry.id, entry.deepCopy());
        this.fsVersion = fsVersion;
        log("Got update " + showFileEntryExtended(entry) + " from server ID: " + serverID);
    }
    
    // Broadcast all updated entries to other servers.
    public synchronized void broadcastUpdateEntry(FileEntryExtended entry) {
        if (mode == Mode.SLAVE)
            return;
        String msg = "Updated entry: " + showFileEntryExtended(entry);
        log(msg);

        fsVersion++;
        for (MasterMasterConnection conn : masterList) {
            int retries = 0;
            while (retries < 2) {
                try {
                    conn.getService().updateEntry(serverID, fsVersion, entry);
                    log("Broadcasted to " + conn.getHostAddress() + ":" + conn.getHostPort());
                    break;
                } catch (TException e) {
                    conn.reopen();
                    retries++;
                }
            }
            if (retries == 2) {
                log("Can't broadcast updated entry to " + conn.getHostAddress() + ":" + conn.getHostPort());
            }
        }
    }
    
    public synchronized void updateCreateEntry(int serverID, long fsVersion, FileEntryExtended entry) {
        // If got message from lower priority server, start election.
        if (serverID > this.serverID) {
            startElection();
            return;
        }

        // If file system version is incorrect, we need to download whole
        // snapshot
        if (fsVersion != this.fsVersion + 1) {
            recreateFileSystem(coordServerID);
            return;
        }

        // Update current file system
        FileEntryExtended parent = idMap.get(entry.entry.parentID);
        if (parent == null)
            return;

        if (entry.entry.type == FileType.DIRECTORY) {
            try {
                FileEntry newDir = makeDirectory2(false, parent.entry, entry.entry.name);
                FileEntryExtended newDirExtended = idMap.get(newDir.id);
                newDirExtended.entry = entry.entry.deepCopy();
                newDirExtended.mirrors = new ArrayList<>(entry.mirrors);
                newDirExtended.state = entry.state;
            } catch (EntryNotFound | InvalidOperation | HostNotPermitted e) {
                log("Critical implementation error in updateCreateEntry");
                e.printStackTrace();
            }
        } else {
            try {
                FileEntry newFile = makeFile2(false, parent.entry, entry.entry.name);
                FileEntryExtended newFileExtended = idMap.get(newFile.id);
                newFileExtended.entry = entry.entry.deepCopy();
                newFileExtended.mirrors = new ArrayList<>(entry.mirrors);
                newFileExtended.state = entry.state;
            } catch (EntryNotFound | InvalidOperation | HostNotPermitted e) {
                log("Critical implementation error in updateCreateEntry()");
                e.printStackTrace();
            }
        }
        this.fsVersion = fsVersion;
        log("Got create update " + showFileEntryExtended(entry) + " from server ID: " + serverID);
    }

    public synchronized void updateRemoveEntry(int serverID, long fsVersion, FileEntryExtended entry) {
        // If got message from lower priority server, start election.
        if (serverID > this.serverID) {
            startElection();
            return;
        }

        // If file system version is incorrect, we need to download whole
        // snapshot
        if (fsVersion != this.fsVersion + 1) {
            recreateFileSystem(coordServerID);
            return;
        }

        // Update current file system
        try {
            removeEntry2(false, entry.entry);
        } catch (EntryNotFound | InvalidOperation | HostNotPermitted e) {
            log("Critical implementation error in updateRemoveEntry()");
            e.printStackTrace();
        }

        this.fsVersion = fsVersion;
        log("Got remove update " + showFileEntryExtended(entry) + " from server ID: " + serverID);
    }

    public synchronized void updateMoveEntry(int serverID, long fsVersion, FileEntryExtended oldEntry, FileEntryExtended newEntry) {
        // If got message from lower priority server, start election.
        if (serverID > this.serverID) {
            startElection();
            return;
        }

        // If file system version is incorrect, we need to download whole
        // snapshot
        if (fsVersion != this.fsVersion + 1) {
            recreateFileSystem(coordServerID);
            return;
        }

        // Update current file system
        FileEntryExtended parent = idMap.get(newEntry.entry.parentID);
        if (parent == null)
            return;

        try {
            FileEntry newEntry2 = moveEntry2(false, oldEntry.entry, parent.entry, newEntry.entry.name);
            FileEntryExtended newEntryExtended = idMap.get(newEntry2.id);
            newEntryExtended.entry = newEntry.entry.deepCopy();
            newEntryExtended.mirrors = new ArrayList<>(newEntry.mirrors);
            newEntryExtended.state = newEntry.state;
        } catch (EntryNotFound | InvalidOperation | HostNotPermitted e) {
            log("Critical implementation error in updateMoveEntry()");
            e.printStackTrace();
        }

        this.fsVersion = fsVersion;
        log("Got move update " + showFileEntryExtended(oldEntry) + " -> " + showFileEntryExtended(newEntry) + " from server ID: " + serverID);
    }

    public synchronized FileSystemSnapshot getFileSystemSnapshot(int serverID) throws HostNotPermitted {
        if (serverID != this.serverID) {
            throw new HostNotPermitted(this.serverID, this.coordServerID);
        }
        List<FileEntryExtended> entryList = new ArrayList<FileEntryExtended>();
        for (Entry<Long, FileEntryExtended> entry : idMap.entrySet()) {
            entryList.add(entry.getValue().deepCopy());
        }
        return new FileSystemSnapshot(entryList, fsVersion);
    }

    public synchronized void recreateFileSystem(int copyServerID) {
        if (copyServerID == this.serverID)
            return;
        log("Recreating file system from server ID: " + copyServerID + "...");

        // Find copy server
        FileSystemSnapshot snap = null;
        for (MasterMasterConnection conn : masterList) {
            if (conn.getServerID() == copyServerID) {
                int retries = 0;
                while (retries < 2) {
                    try {
                        snap = conn.getService().getFileSystemSnapshot(copyServerID);
                        break;
                    } catch (TException e) {
                        conn.reopen();
                        retries++;
                    }
                }
                if (retries == 2) {
                    log("Can't recreate file system snapshot: connection lost from " + conn.getHostAddress() + ":" + 
                         conn.getHostPort() + "(" + conn.getServerID() + ")");
                }
                break;
            }
        }

        if (snap == null)
            return;
        recreateFileSystemFromSnapshot(snap);
    }

    public void recreateFileSystemFromSnapshot(FileSystemSnapshot snapshot) {
        this.idMap.clear();
        this.parentIdMap.clear();
        Long maxID = new Long(0);

        for (Iterator<FileEntryExtended> it = snapshot.entries.iterator(); it.hasNext();) {
            FileEntryExtended entry = it.next();
            idMap.put(entry.entry.id, entry);
            if (entry.entry.id > maxID)
                maxID = entry.entry.id;
            if (entry.entry.type == FileType.DIRECTORY) {
                TreeSet<FileEntryExtended> tmpTreeToCopy = new TreeSet<FileEntryExtended>();
                for (Iterator<FileEntryExtended> it2 = snapshot.entries.iterator(); it2.hasNext();) {
                    FileEntryExtended child = it2.next();
                    if (child.entry.id != 0 && child.entry.parentID == entry.entry.id) {
                        tmpTreeToCopy.add(child);
                    }
                }
                parentIdMap.put(entry.entry.id, new TreeSet<FileEntryExtended>(tmpTreeToCopy));
            }
        }
        this.nextId = maxID + 1;
        this.fsVersion = snapshot.version;
        log("Recreated file system from snapshot nr " + snapshot.version);
    }

    public synchronized void startElection() {
        log("Election started");
        // Let's assume we are coordinator.
        mode = Mode.MASTER;

        // For each server with higher priority number.
        Long maxCopyFsVersion = new Long(0);
        int copyServerID = 0;
        for (MasterMasterConnection conn : masterList) {
            if (conn.getServerID() < serverID) {
                int retries = 0;
                while (retries < 2) {
                    try {
                        Long copyFsVersion = conn.getService().election(serverID);
                        if (copyFsVersion > maxCopyFsVersion) {
                            maxCopyFsVersion = copyFsVersion;
                            copyServerID = conn.getServerID();
                        }
                        // We handled election form higher priority server.
                        // We should be a slave.
                        mode = Mode.SLAVE;
                        log("Election handled from  " + conn.getHostAddress() + ":" + 
                             conn.getHostPort() + "(" + conn.getServerID() + ")");
                        break;
                    } catch (TException e) {
                        conn.reopen();
                        retries++;
                    }
                }
                if (retries == 2) {
                    log("Can't reach server " + conn.getHostAddress() + ":" + conn.getHostPort() + "(" + conn.getServerID() + ")");
                } else
                    break;
            }
        }

        // Broadcast election to lower priority servers if master
        if (mode == Mode.MASTER) {
            log("Elected as a coordinator");
            for (MasterMasterConnection conn : masterList) {
                if (conn.getServerID() > serverID) {
                    int retries = 0;
                    while (retries < 2) {
                        try {
                            // Check if old server doesn't have newer version of
                            // a file system.
                            Long copyFsVersion = conn.getService().elected(serverID);
                            if (copyFsVersion > maxCopyFsVersion) {
                                maxCopyFsVersion = copyFsVersion;
                                copyServerID = conn.getServerID();
                            }

                            log("Send elected to: " + conn.getHostAddress() + ":" + conn.getHostPort() + "(" + conn.getServerID() + ")");
                            break;
                        } catch (TException e) {
                            conn.reopen();
                            retries++;
                        }
                    }
                    if (retries == 2) {
                        log("Couldn't send elected to " + conn.getHostAddress() + ":" + conn.getHostPort() + "(" + conn.getServerID() + ")");
                    }
                }
            }
        } else {
            log("Elected as a slave");
        }
        if (maxCopyFsVersion > fsVersion) {
            recreateFileSystem(copyServerID);
        }
    }

    public synchronized Long election(int serverID) {
        log("Got election from server ID: " + serverID);
        // If server with lower priority starts election do the same.
        // if (serverID > this.serverID) {
        // startElection();
        // }
        return fsVersion;
    }

    public synchronized Long elected(int serverID) {
        log("Got elected from server ID: " + serverID);

        // If server with lower priority elects itself, start election.
        if (serverID > this.serverID) {
            startElection();
        } else {
            // Update information about coordinator
            this.coordServerID = serverID;
            mode = Mode.SLAVE;
            log("New coordinator is: " + serverID);
        }

        return fsVersion;
    }

    public synchronized FileEntryExtended checkIfEntryIsWriteReadReady(FileEntry entry) throws EntryNotFound, InvalidOperation {
        FileEntryExtended extendedEntry = idMap.get(entry.id);
        if (extendedEntry == null || extendedEntry.entry.type != FileType.FILE) {
            throw new EntryNotFound(13, "Entry not found or is not a file: " + entry.name);
        }
        if (extendedEntry.entry.version != entry.version) {
            throw new InvalidOperation(14, "Version of file is not correct");
        }
        if (extendedEntry.state == FileState.MODIFIED || extendedEntry.state == FileState.PREMODIFIED) {
            throw new InvalidOperation(15, "Cannot modify file - someone else is modyfing actually");
        }
        return extendedEntry.deepCopy();
    }
    
    public synchronized FileEntryExtended checkIfEntryIsAllocateReady(FileEntry entry) throws EntryNotFound, InvalidOperation {
        FileEntryExtended extendedEntry = idMap.get(entry.id);
        if (extendedEntry == null || extendedEntry.entry.type != FileType.FILE) {
            throw new EntryNotFound(13, "Entry not found or is not a file: " + entry.name);
        }
        if (extendedEntry.entry.version != entry.version) {
            throw new InvalidOperation(14, "Version of file is not correct");
        }
        if (extendedEntry.state != FileState.IDLE) {
            throw new InvalidOperation(15, "Someone is using a file");
        }
        return extendedEntry.deepCopy();
    }

    public synchronized int getNextTransactionToken() {
        return transactionToken++;
    }

    public synchronized void setFileSize(FileEntry file, long size) {
        FileEntryExtended extendedEntry = idMap.get(file.id);
        extendedEntry.entry.size = size;
        broadcastUpdateEntry(extendedEntry);
    }

    public synchronized Transaction getNewTransaction(FileEntry file, int serverID,  TransactionType type, long offset, long num) throws EntryNotFound, InvalidOperation {
        FileEntryExtended extendedEntry = checkIfEntryIsWriteReadReady(file);
        Transaction transaction = new Transaction(type, getNextTransactionToken(), serverID, this.serverID, offset, num, extendedEntry.entry.id);
        if (fileTransactions.get(extendedEntry.entry.id) == null) {
            fileTransactions.put(extendedEntry.entry.id, new ArrayList<Transaction>());
        }
        fileTransactions.get(extendedEntry.entry.id).add(transaction);
        if (type.equals(TransactionType.WRITE)){
            if (extendedEntry.state == FileState.IDLE) {
                extendedEntry.state = FileState.MODIFIED;
            } else if (extendedEntry.state == FileState.READ) {
                extendedEntry.state = FileState.PREMODIFIED;
            }
        } else if (type.equals(TransactionType.READ)){
            if (extendedEntry.state == FileState.IDLE) {
                extendedEntry.state = FileState.READ;
            }
        }
        log("Created transaction with token " + transaction.token + ", type: " + transaction.type +
            ", from: " + transaction.masterServerID + ", to: " + transaction.dataServerID);
        return transaction;
    }
    
    public synchronized void removeFinishedTransaction(Transaction transaction, boolean isSuccessful) 
            throws InvalidOperation, TException {
        log("Transaction finished with token: " + transaction.token + " and status: " + isSuccessful);
        
        //Find transaction
        ArrayList<Transaction> transactions = (ArrayList<Transaction>) fileTransactions.get(transaction.fileID);
        for (Transaction t : transactions) {
            if (t.token == transaction.token) {
                transactions.remove(t);
                break;
            }
        }
        
        // Update file state
        updateFileState(transaction.fileID);
        FileEntryExtended file = idMap.get(transaction.fileID);
        if (file == null) {
            throw new InvalidOperation(697, "Possible error in removeFinishedTransaction impl!");
        }
        
        // If idle apply changes.
        if (file.state == FileState.IDLE &&
            transaction.type == TransactionType.WRITE &&
            isSuccessful) {
            MasterDataConnection conn = new MasterDataConnection(transaction.dataServerID);
            log("Updating file " + file.entry.id + " to data server " + conn.getHostAddress() + ":" +
                conn.getHostPort());
            if (conn.wasCreated()) {
                conn.getService().applyChanges(transaction.fileID);
                file.entry.version++;
                broadcastUpdateEntry(file);
            }
        }
        
    }
    
    public synchronized void updateFileState(long fileID) {
        ArrayList<Transaction> transactions = (ArrayList<Transaction>) fileTransactions.get(fileID);
        if (transactions == null) {
            idMap.get(fileID).state = FileState.IDLE;
            return;
        }
        
        // Check if we got writers/readers
        boolean isReader = false;
        boolean isWriter = false;
        for (Transaction t : transactions) {
            if (t.type == TransactionType.WRITE) isWriter = true;
            if (t.type == TransactionType.READ) isReader = true;
        }
        
        if (!isReader && !isWriter) {
            idMap.get(fileID).state = FileState.IDLE;
        }
        
        if (isReader && !isWriter) {
            if (idMap.get(fileID).state == FileState.PREMODIFIED)
                idMap.get(fileID).state = FileState.PREMODIFIED;
            else
                idMap.get(fileID).state = FileState.READ;
        }
    }
}
