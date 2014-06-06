/*******************************************************************************
 * RSO
 * Service for Client - Data Server communication.
 ******************************************************************************/
include "DataTypes.thrift"
namespace * rso.at

// Service provided by a Data Server to a client.
service ClientDataService
{
    // Get next file part from a server (read from file)
    // First argument is a transaction got from master server which is used
    // during full transmission of a file.
    // Second argument is information about file part. If first file part is
    // retrieved this structure should be initialized with zeroes. Otherwise
    // FilePart.PartInfo should be used from last call.
    //
    // Returned value is a file chunk of certain size.
    DataTypes.FileChunk getNextFileChunk(1: DataTypes.Transaction transaction, 
                                         2: DataTypes.ChunkInfo chunkInfo)
        throws(1: DataTypes.InvalidOperation err1,
               2: DataTypes.HostNotPermitted err2),

    // Send next file part to a server (write to file)
    // First argument is a transaction got from master server which is used
    // during full transmission of a file.
    // Second argument is information about status of an upload.
    //
    // Returned value is an download status of a file chunk.
    DataTypes.ChunkInfo sendNextFileChunk(1: DataTypes.Transaction transaction,
                                          2: DataTypes.FileChunk fileChunk)
        throws(1: DataTypes.InvalidOperation err1,
               2: DataTypes.HostNotPermitted err2),
}
