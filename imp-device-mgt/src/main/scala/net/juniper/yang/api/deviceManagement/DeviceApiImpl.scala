package net.juniper.yang.api.deviceManagement

import net.juniper.easyrest.ctx.ApiContext
import net.juniper.easyrest.persistence.{ DbaHelper, SqlSessionManager }
import net.juniper.easyrest.yang.mapping.YangMappingDbaQuery
import net.juniper.yang.mo.deviceManagement.Device
import net.juniper.yang.mo.ietfYangTypes.Uuid

import scala.concurrent.{ Future, ExecutionContext }

class DeviceApiImpl extends DeviceApi {
  def getDeviceList(apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Seq[Device]]] = {
    SqlSessionManager[Seq[Device]]("MySqlDS").openSession {
      session =>
        {
          val yangMappingDba = YangMappingDbaQuery(session, QueryDeviceOrm, apiCtx)
          DbaHelper.execute(yangMappingDba)
        }
    }
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

  def deleteDevice(uuid: Uuid, apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Unit]] = {
    Future {
      Some(Unit)
    }
  }
}
