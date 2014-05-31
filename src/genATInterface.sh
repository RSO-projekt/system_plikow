#!/bin/bash
thrift --gen java DataTypes.thrift
thrift --gen java MasterMaster.thrift
thrift --gen java ClientMaster.thrift
thrift --gen java ClientData.thrift
thrift --gen java MasterData.thrift 
thrift --gen java DataMaster.thrift 

