package net.juniper.ems

import net.juniper.easyrest.boot.Bootstrap
import net.juniper.easyrest.core.EasyRestActor
import net.juniper.easyrest.core.EasyRestSubSystemInit
import net.juniper.easyrest.persistence.DatabaseSupport
import net.juniper.easyrest.rest.KeystoneRouting
import net.juniper.yang.EmsServerAllRoutes

object EmsBoot extends App {
  new EmsBootstrap().start()
}

class EmsBootstrap extends Bootstrap[EmsActor] with DatabaseSupport with EasyRestSubSystemInit

//This actor should be generated from Yang
class EmsActor extends EasyRestActor with EmsServerAllRoutes {
  def getRoute = emsServerAllRoutes
}