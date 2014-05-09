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
    // Every change in a metadata should be broadcasted to every mirror
    // Master Server. It's an one way operation. [Called by current Master
    // Server].
    oneway void updateEntry(1: DataTypes.FileEntry entry, 2: i64 modNumber),

    // It can happen that server will be shutdown for a long time and it's
    // not possible to resync with master on delta basis. It's a function
    // to transmit all metadata in [compressed], binary fromat. [Called by
    // mirror Master Server].
    list<DataTypes.FileEntryExtended> updateMetadata(),

    //------ ELECTION FUNCTIONS ------------------------------------------------

    // Message is sent on server initialization and when connection to a
    // coordinator is lost. Election message is only send to server with higher
    // priority.
    void election(1: i32 serverID),

    // Message is sent by a server when no one is responding after an election.
    void elected(1: i32 serverID),

    // Message is sent to stop election for lower priority server.
    void ok(1: i32 serverID)
}
