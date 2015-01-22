package net.juniper.yang.api.deviceManagement

import net.juniper.easyrest.yang.mapping.YangMappingQueryDSL
import net.juniper.yang.mo.deviceManagement.Device

object QueryDeviceOrm extends YangMappingQueryDSL {
  mapping(_ => new Device)
  select("distinct d.id") mapping "uuid"
  //select("d.domainId") mapping "domainId"
  //  select("d.type") 					  mapping "deviceType"
  select("d.hostname") mapping "system/hostname"
  select("d.SerialNumber") mapping "system/serial"
  select("d.deviceFamily") mapping "system/family"
  select("d.SoftwareRelease") mapping "system/osVersion"
  select("d.platform") mapping "system/platform"
  select("d.vendor") mapping "system/vendor"
  //  select("d.lastRebootedTimestamp") mapping "system/lastRebootTime"
  select("cm.ip") mapping "system/ip" fromIndex 3
  select("cm.connectionType") mapping "mgtConnection/type" fromIndex 3 mappingFunc {
    (value, obj, apiCtx) =>
      "UNREACHABLE"
  }
  select("dc.connStatus") mapping "mgtConnection/status" fromIndex 1 mappingFunc {
    (value, obj, apiCtx) =>
      if (value == null)
        "NA"
      else if (value == "0")
        "UP"
      else if (value == "1")
        "DOWN"
      else
        "NA"
  }
  select("dc.authenticationStatus") mapping "mgtConnection/auth" fromIndex 1 mappingFunc {
    (value, obj, apiCtx) =>
      "NOT_AVAILABLE"
  }
  select("d.webMgt") mapping "mgtConnection/webMgt"
  select("d.redundancyGroupStatus") mapping "redundancy/status" mappingFunc {
    (value, obj, apiCtx) =>
      "PRIMARY"
  }
  select("d.dualREStatus") mapping "redundancy/masterRE"
  //  select("d.hostingDevice_id") 		mapping "lsysInfo/lsysRoot/id"
  //  select("d.lsysCount") 			mapping "lsysInfo/lsysMembers/count"
  select("cs.deviceState") mapping "configInfo/configStatus" fromIndex 2 mappingFunc {
    (value, obj, apiCtx) =>
      "NONE"
  }
  select("cs.ccState") mapping "configInfo/candidateConfigState" mappingFunc {
    (value, obj, apiCtx) =>
      "APPROVED"
  }
  select("d.virtualChassisStatus") mapping "virtualChassisStatus" selectOption "optional"
  select("d.trapTarget") mapping "trapTarget" selectOption "optional"
  //  select("do.name") mapping "system/domainname" fromIndex 4
  from("LogicalDevice d")
  from("LEFT JOIN DeviceConnectionStatus dc on dc.device_id_id=d.id")
  from("LEFT JOIN DeviceConfigStatus cs on cs.device_id_id=d.id")
  from("LEFT JOIN DeviceConnectionManagement cm on cm.device_id_id=d.id")
  from("LEFT JOIN DomainEntity do on do.id=d.domainId")
  postProcess {
    (apiCtx, result) => {}
  }
}