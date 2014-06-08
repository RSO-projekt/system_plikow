/*******************************************************************************
 * RSO
 * Service for Data Server - Master Server communication.
 ******************************************************************************/
include "DataTypes.thrift"
namespace * rso.at

// Service provided by a Master Server to a Data Server.
service DataMasterService
{
    // This function is send to master server to inform it that
    // file transaction is finished.
    void transactionFinished(1: DataTypes.Transaction transaction 2: bool isSuccessful)
        throws(1: DataTypes.InvalidOperation err1),

    // Get all mirrored file list that this server should store.
    list<DataTypes.FileEntryExtended> getMirroredFileList()
        throws(1: DataTypes.InvalidOperation err1),
}
