package net.juniper.ems.notifications

import java.io.StringWriter

import akka.actor.{ ActorRef, Props }
import akka.util.Timeout
import net.juniper.easyrest.intergration.messaging.MessagingActor.{ ConsumerCreatedAck, StartConsumerFor }
import net.juniper.easyrest.intergration.messaging.MessagingSubSystem
import net.juniper.easyrest.notification.EasyRestNotificationConstants._
import net.juniper.easyrest.notification.{ EasyRestNotificationHandler, Notification }
import net.juniper.ems.notifications.handlers.{ DatabaseChangeNotification, JmsDbConsumerAndNotificationActor }

import scala.concurrent.{ Await, Future }

/**
 * Implementation of Notification that EMS supports
 */
object NotificationFactory {

  val DB_NOTIFICATION_ENDPOINT = "jms:topic:database-changes"

  def getNotificationHandlerActor(uri: EndPoint): ActorRef = uri match {
    case "database-changes" => getDBChangeNotificationHandlerActor(uri)
    case _ => throw new NotImplementedError(s"The endpoint $uri is not implemented by the server")
  }

  def getNotificationHandler(uri: EndPoint) = uri match {
    case "database-changes" => getDBChangeNotificationHandler(uri)
    case _ => throw new NotImplementedError(s"The endpoint $uri is not implemented by the server")
  }

  private def getDBChangeNotificationHandlerActor(uri: EndPoint): ActorRef = {
    import akka.pattern.ask

    import scala.concurrent.duration._
    implicit val timeout = Timeout(10 second)
    var f: Future[Any] = MessagingSubSystem.messagingActor.ask(StartConsumerFor(Props(classOf[JmsDbConsumerAndNotificationActor[Notification]], DB_NOTIFICATION_ENDPOINT), uri))
    var consumer: ConsumerCreatedAck = Await.result(f, 5 second).asInstanceOf[ConsumerCreatedAck]
    consumer.consumer
  }

  /**
   * DB Notification implementation
   * @param uri
   * @return
   */
  private def getDBChangeNotificationHandler(uri: EndPoint): EasyRestNotificationHandler[_ <: Notification] = {
    new EasyRestNotificationHandler[DatabaseChangeNotification] {
      override val convertToJson: (DatabaseChangeNotification) => String = msg => {
        val writer = new StringWriter()
        msg.toJson(writer, false)
        writer.toString
      }
      override val doFilter: FilterFunction = (msg, filter) => {
        try {
          msg.asInstanceOf[DatabaseChangeNotification].getObjectTypeValue.toString.equals(filter)
        } catch {
          case _: Throwable => false
        }
      }
    }
  }

}
