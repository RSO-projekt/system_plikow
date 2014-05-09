#!/bin/bash
thrift --gen java DataTypes.thrift
thrift --gen java MasterMaster.thrift
thrift --gen java ClientMaster.thrift

