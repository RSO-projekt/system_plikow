package impl.server.master;

import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import rso.at.FileEntry;
import rso.at.FileEntryExtended;
import rso.at.EntryNotFound;
import rso.at.FileState;
import rso.at.FileSystemSnapshot;
import rso.at.FileType;
import rso.at.HostNotPermitted;
import rso.at.InvalidOperation;
import rso.at.MasterMasterService;
import rso.at.MasterMasterService.Iface;

public class FileSystemMonitor {
	// Map containing all IDs of file system's entries.
	private TreeMap<Long, FileEntryExtended> idMap;
	// Map containing all children of parent with specified ID.
	private TreeMap<Long, TreeSet<FileEntryExtended>> parentIdMap;
	// Next available ID.
	private Long nextId;
	// Version of a file system. Every change increment this value by 1.
	private Long fsVersion;
	
	// Internal strings for getParentPath() function.
	private String pathParent;
	private String pathName;
	
	// Current server ID
	int serverID;
	
	// Current coordinator server ID
	int coordServerID;
	
	public void setServerID(int serverID) {
		this.serverID = this.coordServerID = serverID;
	}
	
	enum Mode {
		MASTER,
		SLAVE
	}
	
	// Current mode of a master
	Mode mode;
	
	// List of all redundant master servers
	private ArrayList<MasterConnection> masterList;
	
	// Function checks privileges of a server
	private synchronized void checkPriviliges(boolean external) throws HostNotPermitted {
		if (external && mode == Mode.SLAVE) {
			throw new HostNotPermitted(serverID, coordServerID);
		}
	}
	
	// General logging function
	public synchronized void log(String message) {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		System.out.println("[" + df.format(new Date()) + "] " + message);
	}
	
	// Display short info about an entry
	private String showFileEntryExtended(FileEntryExtended entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + (entry.entry.type == FileType.DIRECTORY ? "DIR: \"" : "FILE: \"") + 
				  entry.entry.name +"\", ");
		sb.append("ID: " + entry.entry.id +", ");
		sb.append("pID: " + entry.entry.parentID + ", ");
		sb.append("ver: " + entry.entry.version + "]");
		return sb.toString();
	}
	
	// Class representing an AT connection. 
	class Connection {
		public Connection(String host, int port, int serverID, String service) {
			transport = new TSocket(host, port, Configuration.sTimeout);
			protocol = new TMultiplexedProtocol(new TBinaryProtocol(transport), service);
			this.host = host;
			this.port = port;
			this.serverID = serverID;
			this.service = service;
		}
		
		public void reopen() {
			if (!transport.isOpen())
				try {
					transport.open();
				} catch (TTransportException e) {
					// It's unrecoverable state of a socket: close it and
					// create a new one.
					transport.close();
					transport = new TSocket(host, port, Configuration.sTimeout);
					protocol = new TMultiplexedProtocol(new TBinaryProtocol(transport), service);
				}
		}
		
		public String getHostAddress() {
			return host;
		}
		
		public int getHostPort() {
			return port;
		}
		
		public int getServerID() {
			return serverID;
		}
		
		protected TTransport transport;
		protected TMultiplexedProtocol protocol;
		protected String host;
		protected int port;
		protected int serverID;
		String service;
	}
	
	// Master server connection.
	class MasterConnection extends Connection {
		public MasterConnection(String host, int port, int priority) {
			super(host, port, priority, "MasterMaster");
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
	
	// Add master connection to the list
	public synchronized void addMasterConnection(String host, int port, int priority) {
		MasterConnection conn = new MasterConnection(host, port, priority);
		masterList.add(conn);
	}
	
	// Private function to easily create new File Entry Extended object 
	private FileEntryExtended createFileEntryExtended(FileType fileType, long time,
													  long parentId, long size, String name)
	{
		FileEntry fe = new FileEntry(fileType, time, nextId, parentId, 0, size, name);
		++nextId;
		return new FileEntryExtended(fe, new ArrayList<Integer>(), FileState.IDLE);
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
		if (path.charAt(path.length()-1) == '/') 
			path = path.substring(0, path.length()-1);
		
		pathParent = path;
		int lastSlash = path.lastIndexOf('/');
		pathParent = path.substring(0, lastSlash);
		if (pathParent.isEmpty()) pathParent = "/";
		
		pathName = path.substring(lastSlash + 1, path.length());
	}
	
	// Default constructor creates root folder.
	public FileSystemMonitor() {
		this.serverID = coordServerID = 0;
		mode = Mode.SLAVE;
		
		nextId = new Long(0);
		fsVersion = new Long(0);

		masterList = new ArrayList<FileSystemMonitor.MasterConnection>();
		idMap = new TreeMap<Long, FileEntryExtended>();
		parentIdMap = new TreeMap<Long, TreeSet<FileEntryExtended>>();
		FileEntryExtended root = createFileEntryExtended(FileType.DIRECTORY, 
														 System.currentTimeMillis() / 1000,
														 0, 0, "root");
		idMap.put(0l, root);
		TreeSet<FileEntryExtended> rootSet = new TreeSet<>();
		parentIdMap.put(0l, rootSet);
	}
	
	// Return file's entry based on it's path.
	public synchronized FileEntryExtended getEntry(boolean external, String path) 
			throws EntryNotFound, HostNotPermitted {
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
		
		for (int i=1; i<pathArray.length;++i) {
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
			throw new EntryNotFound(4, "Directory doesn't exist: " + parent.name );
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
		FileEntryExtended dir = createFileEntryExtended(FileType.DIRECTORY, 
				                                        System.currentTimeMillis() / 1000,
				                                        parent.id, 0, name);
		idMap.put(dir.entry.id, dir);
		parentIdMap.put(dir.entry.id, new TreeSet<FileEntryExtended>());
		parentIdMap.get(parent.id).add(dir);
		
		broadcastCreateEntry(dir);
		return dir.entry.deepCopy();
	}
	
	// Send information about new entry to other, redundant servers.
	public synchronized void broadcastCreateEntry(FileEntryExtended entry) {
		if (mode == Mode.SLAVE) return;
		String msg = "New entry: " + showFileEntryExtended(entry);
		log(msg);
		
		fsVersion++;
		for (MasterConnection conn : masterList) {
			try {
				conn.reopen();
				conn.getService().updateCreateEntry(serverID, fsVersion, entry);
				log("Broadcasted to " + conn.getHostAddress() + ":" + conn.getHostPort());
			} catch (TException e) {
				log("Can't broadcast new entry to " + conn.getHostAddress() + 
					":" + conn.getHostPort());
				e.printStackTrace();
			}
		}
	}
	
	// Create new file in the server.
	public synchronized FileEntry makeFile(boolean external, String path, long size) throws EntryNotFound, InvalidOperation, HostNotPermitted {
		checkPriviliges(external);
		getParentPath(path);
		FileEntryExtended parentEntry = getEntry(false, pathParent);
		return makeFile2(false, parentEntry.entry, pathName, size);
	}
	
	// Create new file in the server by using descriptor.
	public synchronized FileEntry makeFile2(boolean external, FileEntry parent, String name, long size) throws EntryNotFound, InvalidOperation, HostNotPermitted {
		checkPriviliges(external);
		checkParentAndName(parent, name);
		FileEntryExtended file = createFileEntryExtended(FileType.FILE,
														 System.currentTimeMillis() / 1000,
														 parent.id, size, name);
		
		idMap.put(file.entry.id, file);
		parentIdMap.get(parent.id).add(file);
		broadcastCreateEntry(file);
		return file.entry.deepCopy();
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
		if (mode == Mode.SLAVE) return;
		String msg = "Removed entry: " + showFileEntryExtended(entry);
		log(msg);
		
		fsVersion++;
		for (MasterConnection conn : masterList) {
			try {
				conn.reopen();
				conn.getService().updateRemoveEntry(serverID, fsVersion, entry);
				log("Broadcasted to " + conn.getHostAddress() + ":" + conn.getHostPort());
			} catch (TException e) {
				log("Can't broadcast removed entry to " + conn.getHostAddress() +
					":" + conn.getHostPort());
				e.printStackTrace();
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
	public synchronized FileEntry moveEntry2(boolean external, FileEntry entry, FileEntry parent, String name) throws EntryNotFound, InvalidOperation, HostNotPermitted {
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
	public synchronized void broadcastMoveEntry(FileEntryExtended oldEntry,
												FileEntryExtended newEntry) {
		if (mode == Mode.SLAVE) return;
		String msg = "Moved entry: " + showFileEntryExtended(oldEntry) + " -> " +
					 showFileEntryExtended(newEntry);
		log(msg);
		
		fsVersion++;
		for (MasterConnection conn : masterList) {
			try {
				conn.reopen();
				conn.getService().updateMoveEntry(serverID, fsVersion, oldEntry, newEntry);
				log("Broadcasted to " + conn.getHostAddress() + ":" + conn.getHostPort());
			} catch (TException e) {
				log("Can't broadcast moved entry to " + conn.getHostAddress() + 
					":" + conn.getHostPort());
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void updateCreateEntry(int serverID, long fsVersion, FileEntryExtended entry) {
		// If got message from lower priority server, start election.
		if (serverID > this.serverID) {
			startElection();
			return;
		}
		
		// If file system version is incorrect, we need to download whole snapshot
		if (fsVersion != this.fsVersion + 1) {
			recreateFileSystem();
			return;
		}
		
		// Update current file system
		FileEntryExtended parent = idMap.get(entry.entry.parentID);
		if (parent == null) return;
		
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
				FileEntry newFile = makeFile2(false, parent.entry, entry.entry.name, entry.entry.size);
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
		log("Got create update " + showFileEntryExtended(entry) + 
			" from server ID: " + serverID);
	}
	
	public synchronized void updateRemoveEntry(int serverID, long fsVersion, FileEntryExtended entry) {
		// If got message from lower priority server, start election.
		if (serverID > this.serverID) {
			startElection();
			return;
		}
		
		// If file system version is incorrect, we need to download whole snapshot
		if (fsVersion != this.fsVersion + 1) {
			recreateFileSystem();
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
		log("Got remove update " + showFileEntryExtended(entry) + 
			" from server ID: " + serverID);
	}
	
	public synchronized void updateMoveEntry(int serverID, long fsVersion, FileEntryExtended oldEntry, 
			                             FileEntryExtended newEntry) {
		// If got message from lower priority server, start election.
		if (serverID > this.serverID) {
			startElection();
			return;
		}
		
		// If file system version is incorrect, we need to download whole snapshot
		if (fsVersion != this.fsVersion + 1) {
			recreateFileSystem();
			return;
		}
		
		// Update current file system
		FileEntryExtended parent = idMap.get(newEntry.entry.parentID);
		if (parent == null) return;
		
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
		log("Got move update " + showFileEntryExtended(oldEntry) +
			" -> " + showFileEntryExtended(newEntry) +
			" from server ID: " + serverID);
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
	
	public synchronized void recreateFileSystem() {
		if (mode == Mode.MASTER) return;
		if (coordServerID == serverID) return;
		
		// Find coordinator
		FileSystemSnapshot snap = null;
		for (MasterConnection conn : masterList) {
			if (conn.getServerID() == coordServerID) {
				try {
					conn.reopen();
					snap = conn.getService().getFileSystemSnapshot(serverID);
				} catch (TException e) {
					log("Can't recreate file system snapshot: connection lost from " +
				        conn.getHostAddress() + "(" + conn.getServerID() + ")");
					e.printStackTrace();
				}
			}
		}
		
		if (snap == null) return;
		recreateFileSystemFromSnapshot(snap);
	}
	
	public void recreateFileSystemFromSnapshot(FileSystemSnapshot snapshot) {
		this.idMap.clear();
		this.parentIdMap.clear();
		Long maxID = new Long(0);
		
		for (Iterator<FileEntryExtended> it = snapshot.entries.iterator(); it.hasNext();) {
			FileEntryExtended entry = it.next();
			idMap.put(entry.entry.id, entry);
			if (entry.entry.id > maxID) maxID = entry.entry.id;
			if (entry.entry.type == FileType.DIRECTORY) {
				TreeSet<FileEntryExtended> tmpTreeToCopy = new TreeSet<FileEntryExtended>();
				for (Iterator<FileEntryExtended> it2 = snapshot.entries.iterator(); it2.hasNext();) {
					FileEntryExtended child = it2.next();
					if (child.entry.parentID == entry.entry.id){
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
		for (MasterConnection conn : masterList) {
			if (conn.getServerID() < serverID) {
				try {
					conn.reopen();
					conn.getService().election(serverID);
					
					// We handled election form higher priority server.
					// We should be a slave.
					mode = Mode.SLAVE;
					log("Election handled from  " + conn.getHostAddress() +
						"(" + conn.getServerID() + ")");
					break;
				} catch (TException e) {
					log("Can't reach server " + conn.getHostAddress() +
						"(" + conn.getServerID() + ")");
					e.printStackTrace();
				}
			}
		}
		
		// Broadcast election to lower priority servers if master
		if (mode == Mode.MASTER) {
			log("Elected as a coordinator");
			for (MasterConnection conn : masterList) {
				if (conn.getServerID() > serverID) {
					try {
						conn.reopen();
						conn.getService().elected(serverID);
						log("Send elected to: " + conn.getHostAddress() + 
							"(" + conn.getServerID() + ")");
					} catch (TException e) {
						log("Coudn't send elected to " + conn.getHostAddress() +
							"(" + conn.getServerID() + ")");
						e.printStackTrace();
					}
				}
			}
		} else {
			log("Elected as a slave");
		}
		
	}
	
	public synchronized void election(int serverID) {
		log("Got election from server ID: " + serverID);
		// If server with lower priority starts election do the same.
		if (serverID > this.serverID) {
			startElection();
		}
	}
	
	public synchronized void elected(int serverID) {
		log("Got elected from server ID: " + serverID);
		
		// If server with lower priority elects itself, start election.
		if (serverID > this.serverID) {
			startElection();
		} else {
			// Update information about coordinator
			this.coordServerID = serverID;
			mode = Mode.SLAVE;
			log("New coordinator is: "+ serverID);
		}
	}
}


