/*******************************************************************************
 * RSO
 * Service for Client - Master Server communication.
 ******************************************************************************/

// rso = RSO
// cms = Client - Master Server
namespace cpp rso.cms
namespace * rso.cms

enum FileType
{
    FILE,
    DIRECTORY
}

// Main File Entry structure available for a client
struct FileEntry
{
    1: FileType type,        // Entry type: either a DIRECTORY or a FILE
    2: i32 modificationTime, // time in seconds since epoch (modification time)
    3: i64 id,               // unique ID for an entry
    4: i64 parentID,         // unique parent ID
    5: i32 version,          // incremental version number
    6: i64 size,             // total size of an entry
    7: string name           // name of an entry
}

// Exception for non existing entries
exception EntryNotFound
{
    1: i32 code,
    2: string message
}

// Exception for invalid operation
exception InvalidOperation
{
    1: i32 code,
    2: string message
}

// Service provided by a Master Server to a client.
service ClientMasterService
{
    // Find file entry by a string path
    FileEntry getFileEntry(1: string path) throws(1: EntryNotFound err1, 2: InvalidOperation err2),

    // Get list of files for a specific directory
    list<FileEntry> lookup(1: string path) throws(1: EntryNotFound err1, 2: InvalidOperation err2),
    list<FileEntry> lookup2(1: FileEntry parent) throws(1: EntryNotFound err1, 2: InvalidOperation err2),

    // Make a directory.
    void makeDirectory(1: string path) throws(1: EntryNotFound err1, 2: InvalidOperation err2),
    void makeDirectory2(1: FileEntry parent, 2: string name) throws(1: EntryNotFound err1, 2: InvalidOperation err2),

    // Make an empty file with specified size or change size of current file
    // if it exists
    void makeFile(1: string path, 2: i64 size) throws(1: EntryNotFound err1, 2: InvalidOperation err2),
    void makeFilerr2(1: FileEntry parent, 2: string name, 3: i64 size) throws(1: EntryNotFound err1, 2: InvalidOperation err2),

    // Remove entry 
    void removeEntry(1: string path) throws(1: EntryNotFound err1, 2: InvalidOperation err2),
    void removeEntry2(1: FileEntry entry) throws(1: EntryNotFound err1, 2: InvalidOperation err2),

    // Move entry to another node
    void moveEntry(1: string fromPath, 2: string toPath) throws(1: EntryNotFound err1, 2: InvalidOperation err2),
    void moveEntry2(1: FileEntry entry, 2: FileEntry parent, 3: string name) throws(1: EntryNotFound err1, 2: InvalidOperation err2),

    // Write to a file. We are not transmitting bytes yet. This function is
    // only called to create a transaction. It will be completed on connection
    // between Data Server and a client.
    void writeToFile(1: string path, 2: i64 offset, 3: i64 num) throws(1: EntryNotFound err1, 2: InvalidOperation err2),
    void writeToFilerr2(1: FileEntry file, 2: i64 offset, 3: i64 num) throws(1: EntryNotFound err1, 2: InvalidOperation err2),

    // Read from a file. We are not transmitting bytes yet. This function is
    // only called to create a transaction. It will be completed on connection
    // between Data Server and a client.
    void readFromFile(1: string path, 2: i64 offset, 3: i64 num) throws(1: EntryNotFound err1, 2: InvalidOperation err2),
    void readFromFilerr2(1: FileEntry entry, 2: i64 offset, 3: i64 num) throws(1: EntryNotFound err1, 2: InvalidOperation err2),
}
