package net.juniper.ems.notifications

import akka.actor._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import net.juniper.easyrest.boot.Bootstrap
import net.juniper.easyrest.core.{Configurable, EasyRestActionSystem, EasyRestActor}
import net.juniper.easyrest.messaging.MessagingActor.{ConsumerCreatedAck, StartConsumerFor}
import net.juniper.easyrest.messaging.MessagingSubSystem
import net.juniper.easyrest.notification.EasyRestNotificationConstants._
import net.juniper.easyrest.notification._
import net.juniper.easyrest.rest.EasyRestRoutingDSL
import net.juniper.easyrest.rest.EasyRestServerSideEventDirective._
import net.juniper.easyrest.streams.spray.{StreamRegistry, StreamsRoute}
import net.juniper.easyrest.streams.yang.Stream
import net.juniper.ems.generated.DatabaseChangesNotification
import net.juniper.ems.notifications.handlers.JmsDbConsumerAndNotificationActor
import spray.httpx.encoding.Gzip
import spray.routing._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
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

trait MyNotificationSubscriptionManagerSubSystem extends NotificationSubscriptionManagerSubSystem {

  val notificationHandler: EasyRestNotificationHandler[DatabaseChangesNotification] = new EasyRestNotificationHandler[DatabaseChangesNotification] {
    override val convertToJson: (DatabaseChangesNotification) => String = msg => {
      import spray.json._
      import net.juniper.ems.generated.DatabaseChangesNotificationProtocol._
      msg.toJson.toString
    }
    override val doFilter: FilterFunction = (msg, filter) => {
      try {
        msg.asInstanceOf[DatabaseChangesNotification].objType.equals(filter)
      } catch {
        case _: Throwable => false
      }
    }
  }

  override def getNotificationHandler(uri:EndPoint) = notificationHandler
  /**
   * For each endpoint an actor that handles notification. The actor will push all notification messages to a subject.
   * This subject is connected through the {@link ConnectSubject} message
   * @param uri
   * @return
   */
  override def getNotificationHandlerActor(uri: EndPoint): ActorRef = {
    import akka.pattern.ask

import scala.concurrent.duration._
    implicit val timeout = Timeout(10 second)
    var f: Future[Any] = MessagingSubSystem.messagingActor.ask(StartConsumerFor(Props(classOf[JmsDbConsumerAndNotificationActor[Notification]], "jms:topic:database-changes"), uri))
    var consumer: ConsumerCreatedAck = Await.result(f, 1 second).asInstanceOf[ConsumerCreatedAck]
    consumer.consumer
  }
}

class TestActor extends EasyRestActor with TestStreamRoutes {
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
      } ~
      path("stream") {
        get {
          compressResponse(Gzip) {
            sse { (channel, lastEventId, ctx) =>
            {
              var categoryFilter = ctx.request.uri.query.get("obj-category")
              NotificationSubscriptionManager.addSubscriber(channel, "database-changes", categoryFilter)
            }
            }
          }
        }
      }
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

class TestSystem extends Bootstrap[TestActor] with MyNotificationSubscriptionManagerSubSystem with LazyLogging

