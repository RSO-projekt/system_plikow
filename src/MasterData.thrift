/*******************************************************************************
 * RSO
 * Service for Master Server - Data Server communication.
 ******************************************************************************/
include "DataTypes.thrift"
namespace * rso.at

// Service provided by a Data Server to a Master Server.
service MasterDataService 
{
    // If file doesn't exist create a new empty file, otherwise change it's
    // size.
    void allocateFile(1: i64 fileID, 2: i64 newFileSize)
        throws(1: DataTypes.InvalidOperation err1),

    // Prepare file transaction.
    // Master server should create random token and send all transaction
    // parameters to data server. Then data server should check it's space
    // and other constraints before accepting this message. Once function
    // is called without exceptions. Transaction should be saved in master
    // server memory.
    void createFileTransaction(1: DataTypes.Transaction transaction,
                               2: i64 fileID,
                               3: i64 offset,
                               4: i64 size)
        throws(1: DataTypes.InvalidOperation err1),

    // Apply all waiting changes on a file.
    // Once file is moving from any state to an IDLE state this function should
    // be called by master server.
    void applyChanges(1: i64 fileID)
        throws(1: DataTypes.InvalidOperation err1),

}
