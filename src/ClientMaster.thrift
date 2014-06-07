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
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),

    // Get list of files for a specific directory
    list<DataTypes.FileEntry> lookup(1: string path) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),
    list<DataTypes.FileEntry> lookup2(1: DataTypes.FileEntry parent) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),

    // Make a directory.
    DataTypes.FileEntry makeDirectory(1: string path) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),
    DataTypes.FileEntry makeDirectory2(1: DataTypes.FileEntry parent, 2: string name) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),

    // Make an empty file
    DataTypes.FileEntry makeFile(1: string path) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),
    DataTypes.FileEntry makeFile2(1: DataTypes.FileEntry parent, 2: string name) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),
               
    // Allocate file
    DataTypes.FileEntry allocateFile(1: string path, 2: i64 size)
   		throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),
    DataTypes.FileEntry allocateFile2(1: DataTypes.FileEntry file, 2: i64 size)
   		throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),

    // Remove entry 
    void removeEntry(1: string path) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),
    void removeEntry2(1: DataTypes.FileEntry entry) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),

    // Move entry to another node
    DataTypes.FileEntry moveEntry(1: string fromPath, 2: string toPath) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),
    DataTypes.FileEntry moveEntry2(1: DataTypes.FileEntry entry, 2: DataTypes.FileEntry parent, 3: string name) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),

    // Write to a file. We are not transmitting bytes yet. This function is
    // only called to create a transaction. It will be completed on connection
    // between Data Server and a client.
    DataTypes.Transaction writeToFile2(1: DataTypes.FileEntry file, 2: i64 offset, 3: i64 num) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),

    // Read from a file. We are not transmitting bytes yet. This function is
    // only called to create a transaction. It will be completed on connection
    // between Data Server and a client.
    DataTypes.Transaction readFromFile(1: string path, 2: i64 offset, 3: i64 num) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),
    DataTypes.Transaction readFromFile2(1: DataTypes.FileEntry entry, 2: i64 offset, 3: i64 num) 
        throws(1: DataTypes.EntryNotFound err1, 
               2: DataTypes.InvalidOperation err2,
               3: DataTypes.HostNotPermitted err3),
}
