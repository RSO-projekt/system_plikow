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

struct FileEntry
{
    1: FileType type,
    2: i32 time,
    3: i64 id,
    4: i32 version,
    5: i64 size,
    6: string name
}

service ClientMasterService
{
    FileEntry getFileEntry(1: string path),

    list<FileEntry> lookup(1: string path),
    list<FileEntry> lookup(1: FileEntry parent),

    void makeDirectory(1: string path),
    void makeDirectory(1: FileEntry parent, 2: string name),

    void makeFile(1: string path, 2: i64 size),
    void makeFile(1: FileEntry parent, 2: string name, 3: i64 size),

    void removeEntry(1: string path),
    void removeEntry(1: FileEntry entry),

    void moveEntry(1: string fromPath, 2: string toPath),
    void moveEntry(1: FileEntry entry, 2: FileEntry parent, 3: string name),

    void writeToFile(1: string path, 2: i64 offset, 3: i64 num),
    void writeToFile(1: FileEntry file, 2: i64 offset, 3: i64 num),

    void readFromFile(1: string path, 2: i64 offset, 3: i64 num),
    void readFromFile(1: FileEntry entry, 2: i64 offset, 3: i64 num)

}
