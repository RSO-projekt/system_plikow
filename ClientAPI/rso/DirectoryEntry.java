package rso;

import java.util.ArrayList;

/**
 * Informational class describing properties of a directory.
 * 
 * @author Daniel Pogrebniak
 */
public class DirectoryEntry extends Entry {

	private ArrayList<Entry> children;

	DirectoryEntry(){
		children = new ArrayList<>();
	}
	
	/**
     * Gets list of children.
     * @return List of children.
     */
	public ArrayList<Entry> getChildren() {
		return children;
	}

	/**
     * Adds new child to the list of children.
     * @param child New child in this directory.
     * @return true (as specified by Collection.add)
     */
	public boolean addChild(Entry child) {
		return children.add(child);
	}
	
	/**
	 * Remove specific child from the list of children.
	 * @param child Child to removed.
	 * @return Returns true if this list contained the specified element.
	 */
	public boolean removeChild(Entry child){
		return children.remove(child);
	}
}
