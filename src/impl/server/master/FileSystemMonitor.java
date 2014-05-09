package impl.server.master;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.http.entity.FileEntity;

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
	
	public FileSystemMonitor() {
		idMap = new TreeMap<Long, FileEntryExtended>();
		parentIdMap = new TreeMap<Long, TreeSet<FileEntryExtended>>();
		FileEntry rootEntry = new FileEntry(FileType.DIRECTORY, 
											System.currentTimeMillis()/1000,
											0, 0, 0, 0, "root");
		FileEntryExtended root = new FileEntryExtended(rootEntry, new ArrayList<Integer>(),
				                                       FileState.IDLE);
		idMap.put(0l, root);
		TreeSet<FileEntryExtended> rootSet = new TreeSet<>();
		parentIdMap.put(0l, rootSet);
		nextId=1l;
	}
	
	public synchronized FileEntryExtended getEntry(String path) throws EntryNotFound {
		if (path.isEmpty()) {
			throw new EntryNotFound(0, "Empty string not expected");
		}
		String[] pathArray = path.split("/"); 
		if (pathArray.length!=0 && !pathArray[0].isEmpty()) {
			throw new EntryNotFound(0, "Path should start from root");
		}
		FileEntryExtended fe=idMap.get(0l);
		FileEntryExtended fe2=fe;
		for (int i=1; i<pathArray.length;++i) {
			TreeSet<FileEntryExtended> dir = parentIdMap.get(fe.entry.id);
			if (dir == null) {
				throw new EntryNotFound(1, "Cannot find drectory children");
			}
			for (FileEntryExtended fileEntryExtended : dir) {
				if (fileEntryExtended.entry.name.equals(pathArray[i])) 
					fe2=fileEntryExtended;
			}
			if (fe==fe2)
				throw new EntryNotFound(2, "Cannot find drectory children");
			fe = fe2;
		}
		return fe.deepCopy();
	}
	
	public synchronized List<FileEntry> lookup(String path, FileEntry parent) throws EntryNotFound, InvalidOperation {
		FileEntryExtended entry=null;
		if (!path.isEmpty()) 
			entry = getEntry(path);	
		else if (parent!=null)  
			entry = idMap.get(parent.id);
		if (entry==null)
			throw new EntryNotFound(3, "Parent doesn't exist");
		if (entry.entry.type==FileType.FILE)
			throw new InvalidOperation(0, "File cannot has children");
		
		TreeSet<FileEntryExtended> children = parentIdMap.get(entry.entry.id);
		if (children==null)
			throw new EntryNotFound(4, "Parent doesn't exist");
		List<FileEntry> childrenList = new ArrayList<FileEntry>();
		for (FileEntryExtended fileEntry : children) {
			childrenList.add(fileEntry.entry.deepCopy());
		}
		return childrenList;
	}

	public synchronized FileEntry makeDirectory(String path) throws EntryNotFound, InvalidOperation {
		if (path.isEmpty()) {
			throw new EntryNotFound(0, "Empty string not expected");
		}
		if (path.charAt(path.length()-1)=='/') 
			path =path.substring(0, path.length()-1);
		String parentPath = path;
		if (path.lastIndexOf('/')!=0) {
			parentPath = path.substring(0, path.lastIndexOf('/'));
		}
		else
			parentPath="/";
		String dirName = path.substring(path.lastIndexOf('/')+1, path.length());
		FileEntryExtended parentDir = getEntry(parentPath);
		return makeDirectory2(parentDir.entry, dirName);
	}
	
	public synchronized FileEntry makeDirectory2(FileEntry parent, String name) throws EntryNotFound, InvalidOperation {
		if (name.contains("/"))
			throw new InvalidOperation(2, "You cannot put / in directory name");
		if (parent.type==FileType.FILE)
			throw new InvalidOperation(1, "Invalid path. You cannot create folder in a file");
		FileEntry dirEntry = new FileEntry(FileType.DIRECTORY, 
				   System.currentTimeMillis()/1000,
				   nextId, parent.id, 0, 0, name);
		FileEntryExtended dir = new FileEntryExtended(dirEntry, new ArrayList<Integer>(),
					                                  FileState.IDLE);
		++nextId;
		
		idMap.put(dir.entry.id, dir);
		parentIdMap.put(dir.entry.id, new TreeSet<FileEntryExtended>());
		parentIdMap.get(parent.id).add(dir);
		return dir.entry.deepCopy();
	}
	
	public synchronized FileEntry makeFile(String path, long size) throws EntryNotFound, InvalidOperation {
		if (path.isEmpty()) {
			throw new EntryNotFound(0, "Empty string is not expected");
		}
		if (path.charAt(path.length()-1)=='/') 
			path =path.substring(0, path.length()-1);
		String parentPath = path.substring(0, path.lastIndexOf('/'));
		String fileName = path.substring(path.lastIndexOf('/')+1, path.length());
		FileEntryExtended parentEntry = getEntry(parentPath);
		return makeFile2(parentEntry.entry, fileName, size);
	}
	
	public synchronized FileEntry makeFile2(FileEntry parent, String name, long size) throws EntryNotFound, InvalidOperation {
		if (name.contains("/"))
			throw new InvalidOperation(4, "You cannot put / in file name");
		if (parent.type==FileType.FILE)
			throw new InvalidOperation(3, "Invalid path. You cannot create file in a file");
		FileEntry fileEntry = new FileEntry(FileType.FILE, 
											System.currentTimeMillis()/1000,
											nextId, parent.id, 0, size, name);
		FileEntryExtended file = new FileEntryExtended(fileEntry, new ArrayList<Integer>(),
							   						   FileState.IDLE);
		++nextId;
		
		idMap.put(file.entry.id, file);
		parentIdMap.get(parent.id).add(file);
		return file.entry.deepCopy();
	}
	
	public synchronized void removeEntry(String path) throws EntryNotFound, InvalidOperation {
		if (path.isEmpty()) {
			throw new EntryNotFound(0, "Empty string is not expected");
		}
		FileEntryExtended removingEntry = getEntry(path);
		removeEntry2(removingEntry.entry);
	}
	
	public synchronized void removeEntry2(FileEntry entry) throws EntryNotFound, InvalidOperation {
		FileEntryExtended removingEntry = idMap.get(entry.id);
		TreeSet<FileEntryExtended> parentTreeSet = parentIdMap.get(removingEntry.entry.id);
		if (removingEntry.entry.type == FileType.DIRECTORY){
			if(!parentTreeSet.isEmpty()){
				throw new InvalidOperation(5, "Directory is not empty, you cannot remove unempty directory");
			}
		}
		parentTreeSet.remove(removingEntry);
		idMap.remove(removingEntry.entry.id);
	}
	
	public synchronized FileEntry moveEntry(String fromPath, String toPath) throws EntryNotFound, InvalidOperation {
		FileEntryExtended from = getEntry(fromPath);
		FileEntryExtended to;
		try {
			to = getEntry(toPath);
			return moveEntry2(from.entry, to.entry, null);
		} catch (Exception e) {
			String parentPath = getParentPath(toPath);
			to = getEntry(parentPath);
			String name =  toPath.substring(toPath.lastIndexOf('/')+1, toPath.length());
			return moveEntry2(from.entry, to.entry, name);
		}
	}
	
	public synchronized FileEntry moveEntry2(FileEntry entry, FileEntry parent, String name) throws EntryNotFound, InvalidOperation {
		if (name.contains("/"))
			throw new InvalidOperation(4, "You cannot put / in file name");
		if (parent.type == FileType.FILE){
			throw new InvalidOperation(6, "Place to move cannot be file");
		}
		TreeSet<FileEntryExtended> fromTree = parentIdMap.get(entry.parentID);
		TreeSet<FileEntryExtended> toTree = parentIdMap.get(parent.id);
		FileEntryExtended movingEntry = idMap.get(entry.id);
		fromTree.remove(movingEntry);
		toTree.add(movingEntry);
		movingEntry.entry.parentID = parent.id;
		if (!name.isEmpty()){
			movingEntry.entry.name = name;
		}
		return movingEntry.entry.deepCopy();
	}
	
	private String getParentPath(String path) {
		if (path.charAt(path.length()-1)=='/') 
			path =path.substring(0, path.length()-1);
		String parentPath = path;
		if (path.lastIndexOf('/')!=0) {
			parentPath = path.substring(0, path.lastIndexOf('/'));
		}
		else
			parentPath="/";
		return parentPath;
	}
}


