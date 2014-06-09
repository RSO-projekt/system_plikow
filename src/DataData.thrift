/*******************************************************************************
 * RSO
 * Service for Data Server - Data Server communication.
 ******************************************************************************/
include "DataTypes.thrift"
namespace * rso.at

// Service provided by a Data Server to a Data Server.
service DataDataService 
{
	// If file doesn't exist create a new empty file, otherwise change it's
    // size.
    void allocateFile(1: DataTypes.FileEntryExtended file, 2: i64 newFileSize)
        throws(1: DataTypes.InvalidOperation err1),
        
    void sendFileChunk(1: DataTypes.Transaction transaction,
                       2: DataTypes.FileChunk fileChunk)
        throws(1: DataTypes.InvalidOperation err1),
        
    void applyChanges(1: i64 fileID)
        throws(1: DataTypes.InvalidOperation err1),
        
    binary getFile(1: DataTypes.FileEntryExtended file)
        throws(1: DataTypes.InvalidOperation err1),
}
