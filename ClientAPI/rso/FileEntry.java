package rso;

import java.util.Date;

/**
 * Informational class describing properties of a file. Each
 * FileEntry uniquely identifies file based on theirs ID and
 * version number.
 * 
 * @author Przemysław Lenart
 */
public class FileEntry extends Entry {
	
	private Date modificationDate;
	private long version;
    private long size;
	
	/**
     * Gets date of last modification.
     * @return Date of last modification.
     */
    public Date getModificationDate() {
        return modificationDate;
    }

    /**
     * Sets date of last modification.
     * @param modificationDate Date of last modification.
     */
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
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
     * Increment entry's version.
     */
    public void incrementVersion(){
    	++version;
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
}
