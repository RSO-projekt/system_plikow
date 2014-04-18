package rso;

import java.text.SimpleDateFormat;

/**
 * Informational class describing properties of a file or directory. Each
 * FileEntry uniquely identifies file or directory based on theirs ID and
 * version number.
 * 
 * @author Przemysław Lenart
 */
public class FileEntry {
	
	/**
	 * Type of an entry. Can be file or directory.
	 * @author Przemysław Lenart
	 */
	public enum Type {
		FILE,
		DIRECTORY
	}

	/**
	 * Gets type of an entry.
	 * @return Type of an entry. Can be file or directory.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Sets type of an entry.
	 * @param Type of an entry. Can be file or directory.
	 */
	public void setType(Type type) {
		this.type = type;
	}
	
	/**
	 * Gets date of last modification.
	 * @return Date of last modification.
	 */
	public SimpleDateFormat getModificationDate() {
		return modificationDate;
	}

	/**
	 * Sets date of last modification.
	 * @param modificationDate Date of last modification.
	 */
	public void setModificationDate(SimpleDateFormat modificationDate) {
		this.modificationDate = modificationDate;
	}

	/**
	 * Gets entry's unique identifier.
	 * @return Entry's unique identifier.
	 */
	public long getID() {
		return id;
	}

	/**
	 * Sets entry's unique identifier.
	 * @param id Unique entry's identifier.
	 */
	public void setID(long id) {
		this.id = id;
	}

	/**
	 * Gets entry's parent's unique identifier.
	 * @return Entry's unique identifier.
	 */
	public long getParentID() {
		return parentID;
	}

	/**
	 * Sets entry's parent's unique identifier.
	 * @param id Unique entry's identifier.
	 */
	public void setParentID(long id) {
		this.parentID = id;
	}

	/**
	 * Gets entry's version, describing number of total changes done to a file.
	 * @return Entry's version.
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Sets entry's version, describing number of total changes done to a file.
	 * @param version Entry's version.
	 */
	public void setVersion(long version) {
		this.version = version;
	}

	/**
	 * Gets total length of a file.
	 * @return File size.
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Sets size of a file.
	 * @param size File size.
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Gets entry's name.
	 * @return Entry's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets entry's name.
	 * @param name Entry's name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	private Type type;
	private SimpleDateFormat modificationDate;
	private long id;
    private long parentID;
	private long version;
	private long size;
	private String name;
}
