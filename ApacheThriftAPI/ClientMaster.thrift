/*******************************************************************************
 * RSO
 * Service for Client - Master Server communication.
 ******************************************************************************/
include "DataTypes.thrift"
namespace * rso.at

// Service provided by a Master Server to a client.
service ClientMasterService
{
    // Find file entry by a string path
    DataTypes.FileEntry getFileEntry(1: string path) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),

    // Get list of files for a specific directory
    list<DataTypes.FileEntry> lookup(1: string path) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),
    list<DataTypes.FileEntry> lookup2(1: DataTypes.FileEntry parent) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),

    // Make a directory.
    DataTypes.FileEntry makeDirectory(1: string path) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),
    DataTypes.FileEntry makeDirectory2(1: DataTypes.FileEntry parent, 2: string name) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),

    // Make an empty file with specified size or change size of current file
    // if it exists
    DataTypes.FileEntry makeFile(1: string path, 2: i64 size) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),
    DataTypes.FileEntry makeFile2(1: DataTypes.FileEntry parent, 2: string name, 3: i64 size) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),

    // Remove entry 
    void removeEntry(1: string path) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),
    void removeEntry2(1: DataTypes.FileEntry entry) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),

    // Move entry to another node
    DataTypes.FileEntry moveEntry(1: string fromPath, 2: string toPath) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),
    DataTypes.FileEntry moveEntry2(1: DataTypes.FileEntry entry, 2: DataTypes.FileEntry parent, 3: string name) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),

    // Write to a file. We are not transmitting bytes yet. This function is
    // only called to create a transaction. It will be completed on connection
    // between Data Server and a client.
    void writeToFile(1: string path, 2: i64 offset, 3: i64 num) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),
    void writeToFile2(1: DataTypes.FileEntry file, 2: i64 offset, 3: i64 num) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),

    // Read from a file. We are not transmitting bytes yet. This function is
    // only called to create a transaction. It will be completed on connection
    // between Data Server and a client.
    void readFromFile(1: string path, 2: i64 offset, 3: i64 num) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),
    void readFromFile2(1: DataTypes.FileEntry entry, 2: i64 offset, 3: i64 num) 
        throws(1: DataTypes.EntryNotFound err1, 2: DataTypes.InvalidOperation err2),
}
