/*******************************************************************************
 * RSO
 * Service for Master Server - Master Server communication.
 ******************************************************************************/
include "DataTypes.thrift"
namespace * rso.at

// Service provided by a Master Server to another Master Server
service MasterMasterService
{
    //----- SYNCHRONIZATION FUNCTIONS ------------------------------------------
    void updateCreateEntry(1: i32 serverID, 
                           2: i64 fsVersion, 
                           3: DataTypes.FileEntryExtended entry),
    void updateRemoveEntry(1: i32 serverID,
                           2: i64 fsVersion, 
                           3: DataTypes.FileEntryExtended entry),
    void updateMoveEntry(1: i32 serverID,
                         2: i64 fsVersion, 
                         3: DataTypes.FileEntryExtended oldEntry,
                         4: DataTypes.FileEntryExtended newEntry),

    // It can happen that server will be shutdown for a long time and it's
    // not possible to resync with master on delta basis.
    // [Called by mirror Master Server].
    DataTypes.FileSystemSnapshot getFileSystemSnapshot(1: i32 serverID)
        throws (1: DataTypes.HostNotPermitted err),

    //------ ELECTION FUNCTIONS ------------------------------------------------

    // Message is sent on server initialization and when connection to a
    // coordinator is lost. Election message is only send to server with higher
    // priority. Returns current file system version.
    i64 election(1: i32 serverID),

    // Message is sent by a server when no one is responding after an election.
    // Returns current filesystem version.
    i64 elected(1: i32 serverID),
}
