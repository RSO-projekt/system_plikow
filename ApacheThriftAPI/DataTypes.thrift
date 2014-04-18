/******************************************************************************
 * RSO
 * Data Types
 *****************************************************************************/
namespace * rso.at

enum FileType
{
    FILE,
    DIRECTORY
}

enum FileState
{
    IDLE,        // No one is using a file currently
    READ,        // Someone is reading a file
    PREMODIFIED, // Someone is reading a file and it's marked to be modified
                 // Anyone who would like to read it will be blocked.
    MODIFIED     // File is being modified. No one is allowed to do anything to
                 // it as long as it's being finished.
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

// Extended File Entry structure available for a servers
struct FileEntryExtended 
{
    1: FileEntry entry;      // Information visible for normal user
    2: list<i32> mirrors;    // List of server's ID who have a file mirrors.
    3: FileState state;      // State of a file
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
