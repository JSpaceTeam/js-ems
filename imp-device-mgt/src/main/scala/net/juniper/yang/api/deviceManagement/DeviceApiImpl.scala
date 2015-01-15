package net.juniper.yang.api.deviceManagement

import net.juniper.easyrest.ctx.ApiContext
import net.juniper.yang.mo.deviceManagement.Device
import net.juniper.yang.mo.ietfYangTypes.Uuid

import scala.concurrent.{ Future, ExecutionContext }

class DeviceApiImpl extends DeviceApi {
  def getDeviceList(apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Seq[Device]]] = {
    Future {
      val dev1 = new Device("a")
      dev1.setDisplayNameValue("Device1")
      Some(Seq[Device](dev1, new Device("b")))
    }
  }

  def updateDevice(device: Device, apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Device]] = { Future { None } }

  def createDevice(device: Device, apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Device]] = { Future { None } }

  def getDeviceByUuid(uuid: Uuid, apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Device]] = { Future { None } }

  def deleteDevice(uuid: Uuid, apiCtx: ApiContext)(implicit ec: ExecutionContext): Future[Option[Unit]] = { Future { None } }
}
