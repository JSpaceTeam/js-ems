package net.juniper.ems.notifications

import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import net.juniper.easyrest.boot.Bootstrap
import net.juniper.easyrest.core.{ Configurable, EasyRestActionSystem, EasyRestActor }
import net.juniper.easyrest.intergration.messaging.MessagingSubSystem
import net.juniper.easyrest.rest.EasyRestRoutingDSL
import net.juniper.easyrest.streams.spray.{ StreamRegistry, StreamsRoute }
import net.juniper.easyrest.streams.yang.Stream
import net.juniper.easyrest.testkit.EasyRestRouteTest
import net.juniper.yang.api.emsNotifications.EmsNotificationsRoutes
import spray.routing._
import scala.concurrent.duration._
/**
 * Created by jalandip on 11/6/14.
 */
object EventTestApp extends App with Configurable with LazyLogging {

  implicit val system = EasyRestActionSystem.system

  implicit val timeout = Timeout(5.seconds)

  MessagingSubSystem.initStatus(MessagingSubSystem.start(ConfigFactory.parseString("""
       messaging:{
         host: 10.150.113.192
         port: 5445
        }
                                                                                   """)))

  implicit val ex: ExceptionHandler = spray.routing.ExceptionHandler.default

  val server = new TestSystem
  server.start(Map[String, String]("port" -> "8080", "interface" -> "0.0.0.0"))
}

class TestActor extends EasyRestActor with TestStreamRoutes with EmsNotificationsRoutes with EasyRestRouteTest {
  def getRoute: Route = route()

  def route(): Route = {
    pathSingleSlash {
      get {
        getFromResource("eventsourcetest.html")
      }
    } ~
      path("ie") {
        getFromResource("eventsourcetest_ie.html")
      } ~
      path("eventsource.js") {
        getFromResource("eventsource.js")
      } ~ emsNotificationsRestApiRouting
  }
}

trait TestStreamRoutes extends EasyRestRoutingDSL with StreamsRoute with HttpService {

  implicit val routingSetting: RoutingSettings = spray.routing.RoutingSettings.default

  StreamRegistry.registerStream(new Stream()
    .name("database-changes")
    .description("Stream for all database change events")
    .replaySupport("false")
    .events(""))

  def getRoute(sseProcessor: ActorRef) = streamsRestRouting
}

class TestSystem extends Bootstrap[TestActor] with EmsNotifications with LazyLogging

