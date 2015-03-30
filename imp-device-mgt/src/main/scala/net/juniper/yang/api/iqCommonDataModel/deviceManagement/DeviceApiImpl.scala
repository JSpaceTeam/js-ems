package net.juniper.yang.api.iqCommonDataModel.deviceManagement

import net.juniper.easyrest.ctx.{ ApiContext, Page }
import net.juniper.easyrest.persistence.{ DbaHelper, SqlSessionManager }
import net.juniper.easyrest.yang.mapping.YangMappingDbaQuery
import net.juniper.yang.mo.ietfYangTypes.Uuid
import net.juniper.yang.mo.iqCommonDataModel.deviceManagement.Device

import scala.concurrent.{ ExecutionContext, Future }

class DeviceApiImpl extends DeviceApi {
  //TODO will be implementd via EJB invoke
  def getDeviceList(apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Seq[Device]] = {
    SqlSessionManager[Page[Device]]("MySqlDS").openSession {
      session =>
        {
          val yangMappingDba = YangMappingDbaQuery(session, QueryDeviceOrm, apiCtx)
          DbaHelper.execute(yangMappingDba)
        }
    }.map[Seq[Device]] {
      result =>
        result.get.records
    }
  }

  def getDeviceCount(apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Long] = {
    SqlSessionManager[Page[Device]]("MySqlDS").openSession {
      session =>
        {
          val yangMappingDba = YangMappingDbaQuery(session, QueryDeviceOrm, apiCtx)
          DbaHelper.execute(yangMappingDba)
        }
    }.map[Long](
      result =>
        result.get.totalRecords
    )
  }

  def updateDevice(device: Device, apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Device]] = {
    Future {
      val dev = new Device("a")
      dev.setDisplayNameValue("Device was updated")
      Some(dev)
    }
  }

  def createDevice(device: Device, apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Device]] = {
    Future {
      val dev = new Device("b")
      dev.setDisplayNameValue("Device was created")
      Some(dev)
    }
  }

  def getDeviceByUuid(uuid: Uuid, apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Device]] = {
    Future {
      val dev = new Device("a")
      dev.setDisplayNameValue("Device was fetched")
      Some(dev)
    }
  }

  def deleteDevice(uuid: Uuid, apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Boolean] = {
    Future {
      true
    }
  }
}