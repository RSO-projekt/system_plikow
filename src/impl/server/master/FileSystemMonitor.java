package impl.server.master;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import rso.at.FileEntry;
import rso.at.FileEntryExtended;
import rso.at.EntryNotFound;
import rso.at.FileState;
import rso.at.FileType;
import rso.at.InvalidOperation;

public class FileSystemMonitor {
	private TreeMap<Long, FileEntryExtended> idMap;
	private TreeMap<Long, TreeSet<FileEntryExtended>> parentIdMap;
	private Long nextId;
	private Long fsVersion;
	
	private String pathParent;
	private String pathName;
		
	
	private FileEntryExtended createFileEntryExtended(FileType fileType, long time,
													  long parentId, long size, String name)
	{
		FileEntry fe = new FileEntry(fileType, time, nextId, parentId, 0, size, name);
		++nextId;
		return new FileEntryExtended(fe, new ArrayList<Integer>(), FileState.IDLE);
	}
	
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
	
	public FileSystemMonitor() {
		nextId = new Long(0);
		fsVersion = new Long(0);
		idMap = new TreeMap<Long, FileEntryExtended>();
		parentIdMap = new TreeMap<Long, TreeSet<FileEntryExtended>>();
		FileEntryExtended root = createFileEntryExtended(FileType.DIRECTORY, 
														 System.currentTimeMillis() / 1000,
														 0, 0, "root");
		idMap.put(0l, root);
		TreeSet<FileEntryExtended> rootSet = new TreeSet<>();
		parentIdMap.put(0l, rootSet);
	}
	
	public synchronized FileEntryExtended getEntry(String path) throws EntryNotFound {
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
	
	public synchronized List<FileEntry> lookup(String path, FileEntry parent) throws EntryNotFound, InvalidOperation {
		FileEntryExtended entry = null;
		if (!path.isEmpty()) 
			entry = getEntry(path);	
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

	public synchronized FileEntry makeDirectory(String path) throws EntryNotFound, InvalidOperation {
		getParentPath(path);
		FileEntryExtended parentDir = getEntry(pathParent);
		return makeDirectory2(parentDir.entry, pathName);
	}
	
	public synchronized FileEntry makeDirectory2(FileEntry parent, String name) throws EntryNotFound, InvalidOperation {
		checkParentAndName(parent, name);
		FileEntryExtended dir = createFileEntryExtended(FileType.DIRECTORY, 
				                                        System.currentTimeMillis() / 1000,
				                                        parent.id, 0, name);
		idMap.put(dir.entry.id, dir);
		parentIdMap.put(dir.entry.id, new TreeSet<FileEntryExtended>());
		parentIdMap.get(parent.id).add(dir);
		return dir.entry.deepCopy();
	}
	
	public synchronized FileEntry makeFile(String path, long size) throws EntryNotFound, InvalidOperation {
		getParentPath(path);
		FileEntryExtended parentEntry = getEntry(pathParent);
		return makeFile2(parentEntry.entry, pathName, size);
	}
	
	public synchronized FileEntry makeFile2(FileEntry parent, String name, long size) throws EntryNotFound, InvalidOperation {
		checkParentAndName(parent, name);
		FileEntryExtended file = createFileEntryExtended(FileType.FILE,
														 System.currentTimeMillis() / 1000,
														 parent.id, size, name);
		
		idMap.put(file.entry.id, file);
		parentIdMap.get(parent.id).add(file);
		return file.entry.deepCopy();
	}
	
	public synchronized void removeEntry(String path) throws EntryNotFound, InvalidOperation {
		FileEntryExtended removingEntry = getEntry(path);
		removeEntry2(removingEntry.entry);
	}
	
	public synchronized void removeEntry2(FileEntry entry) throws EntryNotFound, InvalidOperation {
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
	}
	
	public synchronized FileEntry moveEntry(String fromPath, String toPath) throws EntryNotFound, InvalidOperation {
		getParentPath(toPath);
		FileEntryExtended parent = getEntry(pathParent);
		FileEntryExtended entry = getEntry(fromPath);
		return moveEntry2(entry.entry, parent.entry, pathName);
	}
	
	public synchronized FileEntry moveEntry2(FileEntry entry, FileEntry parent, String name) throws EntryNotFound, InvalidOperation {
		checkParentAndName(parent, name);
		FileEntryExtended entryExtended = idMap.get(entry.id);
		if (entryExtended == null) {
			throw new InvalidOperation(16, "Entry doesn't exist: " + entry.name);
		}
		
		if (entry.parentID == parent.id) {
			entryExtended.entry.name = name;
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
		return entryExtended.entry.deepCopy();
	}

	public synchronized void updateCreateEntry(long fsVersion, FileEntryExtended entry) {
		if (fsVersion != this.fsVersion + 1) {
			// TODO: Update all metadata
			return;
		}
		
		FileEntryExtended parent = idMap.get(entry.entry.parentID);
		if (parent == null) return;
		
		if (entry.entry.type == FileType.DIRECTORY) {
			try {
				FileEntry newDir = makeDirectory2(parent.entry, entry.entry.name);
				FileEntryExtended newDirExtended = idMap.get(newDir.id);
				newDirExtended.entry = entry.entry.deepCopy();
				newDirExtended.mirrors = new ArrayList<>(entry.mirrors);
				newDirExtended.state = entry.state;
			} catch (EntryNotFound | InvalidOperation e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				FileEntry newFile = makeFile2(parent.entry, entry.entry.name, entry.entry.size);
				FileEntryExtended newFileExtended = idMap.get(newFile.id);
				newFileExtended.entry = entry.entry.deepCopy();
				newFileExtended.mirrors = new ArrayList<>(entry.mirrors);
				newFileExtended.state = entry.state;
			} catch (EntryNotFound | InvalidOperation e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.fsVersion = fsVersion;
	}
	
	public synchronized void updateRemoveEntry(long fsVersion, FileEntryExtended entry) {
		if (fsVersion != this.fsVersion + 1) {
			// TODO: Update all metadata
			return;
		}
		
		try {
			removeEntry2(entry.entry);
		} catch (EntryNotFound | InvalidOperation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.fsVersion = fsVersion;
	}
	
	public synchronized void updateMoveEntry(long fsVersion, FileEntryExtended oldEntry, 
			                             FileEntryExtended newEntry) {
		if (fsVersion != this.fsVersion + 1) {
			// TODO: Update all metadata
			return;
		}
		
		FileEntryExtended parent = idMap.get(newEntry.entry.parentID);
		if (parent == null) return;
		
		try {
			FileEntry newEntry2 = moveEntry2(oldEntry.entry, parent.entry, newEntry.entry.name);
			FileEntryExtended newEntryExtended = idMap.get(newEntry2.id);
			newEntryExtended.entry = newEntry.entry.deepCopy();
			newEntryExtended.mirrors = new ArrayList<>(newEntry.mirrors);
			newEntryExtended.state = newEntry.state;
		} catch (EntryNotFound | InvalidOperation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.fsVersion = fsVersion;
	}
}


