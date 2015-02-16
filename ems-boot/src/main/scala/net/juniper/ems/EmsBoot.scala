package net.juniper.ems

import net.juniper.easyrest.boot.Bootstrap
import net.juniper.easyrest.core.{ EasyRestActor }
import net.juniper.easyrest.persistence.DatabaseSupport
import net.juniper.easyrest.subsystem.EasyRestIntegrationSubsystem
import net.juniper.ems.notifications.EmsNotifications
import net.juniper.yang.EmsServerAllRoutes
import net.juniper.yang.api.commonModule.deviceManagement.DeviceManagementRoutes

object EmsBoot extends App {
  new EmsBootstrap().start()
}

class EmsBootstrap extends Bootstrap[EmsActor] with DatabaseSupport with EasyRestIntegrationSubsystem with EmsNotifications

//This actor should be generated from Yang
class EmsActor extends EasyRestActor with EmsServerAllRoutes with DeviceManagementRoutes {
  def getRoute = emsServerAllRoutes ~ deviceManagementRestApiRouting
}