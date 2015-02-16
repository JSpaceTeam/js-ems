package net.juniper.yang.api.commonModule.deviceManagement

import net.juniper.easyrest.yang.mapping.YangMappingQueryDSL
import net.juniper.yang.mo.commonModule.deviceManagement.Device

object QueryDeviceOrm extends YangMappingQueryDSL {
  mapping(_ => new Device)

  val deviceTable = from("LogicalDevice d")
  val connStatusTable = from("LEFT JOIN DeviceConnectionStatus dc on dc.device_id_id=d.id")
  val configStatusTable = from("LEFT JOIN DeviceConfigStatus cs on cs.device_id_id=d.id")
  val connMgtTable = from("LEFT JOIN DeviceConnectionManagement cm on cm.device_id_id=d.id")
  val domainTable = from("LEFT JOIN DomainEntity do on do.id=d.domainId")

  select("distinct d.id") mapping "uuid"
  //select("d.domainId") mapping "domainId"
  //  select("d.type") 					  mapping "deviceType"
  select("d.hostname") mapping "system/hostname"
  select("d.SerialNumber") mapping "system/serial"
  select("d.deviceFamily") mapping "system/family"
  select("d.SoftwareRelease") mapping "system/os-version"
  select("d.platform") mapping "system/platform"
  select("d.vendor") mapping "system/vendor"
  //  select("d.lastRebootedTimestamp") mapping "system/lastRebootTime"
  select("cm.ip") mapping "system/ip" from connMgtTable
  select("cm.connectionType") mapping "mgt-connection/type" from connMgtTable mappingFunc {
    (value, record, session, apiCtx) =>
      "UNREACHABLE"
  }
  select("dc.connStatus") mapping "mgt-connection/status" from connStatusTable mappingFunc {
    (value, record, session, apiCtx) =>
      if (value == null)
        "NA"
      else if (value == "0")
        "UP"
      else if (value == "1")
        "DOWN"
      else
        "NA"
  }
  select("dc.authenticationStatus") mapping "mgt-connection/auth" from connStatusTable mappingFunc {
    (value, record, session, apiCtx) =>
      "NOT_AVAILABLE"
  }
  select("d.webMgt") mapping "mgt-connection/web-mgt"
  select("d.redundancyGroupStatus") mapping "redundancy/status" mappingFunc {
    (value, record, session, apiCtx) =>
      "PRIMARY"
  }
  select("d.dualREStatus") mapping "redundancy/master-re"
  //  select("d.hostingDevice_id") 		mapping "lsysInfo/lsysRoot/id"
  //  select("d.lsysCount") 			mapping "lsysInfo/lsysMembers/count"
  select("cs.deviceState") mapping "config-info/config-status" from configStatusTable mappingFunc {
    (value, record, session, apiCtx) =>
      "NONE"
  }
  select("cs.ccState") mapping "config-info/candidate-config-state" mappingFunc {
    (value, record, session, apiCtx) =>
      "APPROVED"
  }
  select("d.virtualChassisStatus") mapping "virtual-chassis-status" selectOption "optional"
  select("d.trapTarget") mapping "trap-target" selectOption "optional"
  //  select("do.name") mapping "system/domainname" fromIndex 4

  postProcess((result, session, apiCtx) => {
    //val yangMappingDba = YangMappingDbaQuery(session, QueryDeviceOrm, apiCtx)
    //yangMappingDba.run
  })
}