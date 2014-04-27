package rso;

/**
 * Informational class describing properties of a file or directory. Each
 * Entry uniquely identifies file or directory based on theirs ID.
 * 
 * @author Przemysław Lenart
 */
public abstract class Entry {
    
	private long id;
    private DirectoryEntry parent;
    private String name;
    
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
     * Gets entry's parent.
     * @return Entry's parent.
     */
    public DirectoryEntry getParent() {
        return parent;
    }

    /**
     * Sets entry's parent.
     * @param parent Entry's parent.
     */
    public void setParent(DirectoryEntry parent) {
        this.parent = parent;
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
    
    @Override
    public String toString() {
    	return getName();
    }

}
