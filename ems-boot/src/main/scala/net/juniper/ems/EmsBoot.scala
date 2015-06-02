package net.juniper.ems

import net.juniper.easyrest.boot.Bootstrap
import net.juniper.easyrest.core.EasyRestActor
import net.juniper.easyrest.subsystem.EasyRestIntegrationSubsystem
import net.juniper.ems.notifications.EmsNotifications

object EmsBoot extends App {
  new EmsBootstrap().start()
}

class EmsBootstrap extends Bootstrap[EmsActor] with EasyRestIntegrationSubsystem with EmsNotifications

class EmsActor extends EasyRestActor {
  def getRoute = get {
    complete("")
  }
}