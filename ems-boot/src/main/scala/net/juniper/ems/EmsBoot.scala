package net.juniper.ems

import net.juniper.easyrest.boot.Bootstrap
import net.juniper.easyrest.core.EasyRestActor
import net.juniper.easyrest.core.EasyRestSubSystemInit
import net.juniper.easyrest.persistence.DatabaseSupport

object EmsBoot extends App with DatabaseSupport with EasyRestSubSystemInit {
  new EmsBootstrap().start()
}

class EmsBootstrap extends Bootstrap[EmsActor]

//This actor should be generated from Yang
class EmsActor extends EasyRestActor with DatabaseSupport with EasyRestSubSystemInit {
  def getRoute = pathSingleSlash {
    complete("Ems services")
  }
}