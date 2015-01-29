package net.juniper.ems.notifications.handlers

import akka.camel.CamelMessage
import com.typesafe.scalalogging.LazyLogging
import net.juniper.easyrest.notification.EasyRestNotificationConstants.ConnectSubject
import net.juniper.easyrest.notification.Notification
import net.juniper.jmp.cmp.systemService.security.ManagedObjectInfo
import org.apache.camel.CamelException
import rx.lang.scala.Subject

/**
 * A server notification actor that consumes messages from JMS and forwards it to an Rx Subject.
 * Its upto the subject to then forward it to all sub scribes we use a PublishSubject since any new
 * subscriber is interested only on the events happening after the subscription
 * Created by jalandip on 11/20/14.
 */
class JmsDbConsumerAndNotificationActor[T <: Notification](endPoint: String) extends akka.camel.Consumer with LazyLogging {

  override def endpointUri: String = endPoint

  var subject: Option[Subject[Notification]] = None

  def receive = {
    case ConnectSubject(sub) => {
      subject = sub
      context.become(forwardBehaviour)
    }
    case e: CamelException => {
      subject.map(_.onError(e))
    }
    case x => logger.debug("Messaged received in JMS Consumer " + x)
  }

  def forwardBehaviour: Receive = {
    case msg: CamelMessage => {
      val message = convert(msg)
      logger debug (" Message Received " + message)
      subject.get.onNext(message)
      logger debug (" Message Processed " + message)
    }
  }

  def convert(message: CamelMessage): DatabaseChangeNotification = {
    val msg = message.getBodyAs(classOf[ManagedObjectInfo], camelContext)
    val dbNotification = new DatabaseChangeNotification();
    dbNotification.setEventTimeValue(Notification.getTime());
    dbNotification.setEntityCategoryValue(msg.getEntityCategory)
    dbNotification.setObjectNameValue(msg.getObjName)
    dbNotification.setObjectTypeValue(msg.getObjType)
    dbNotification.setRowIdValue(msg.getRowId)
    dbNotification.setUuidValue(s"000000${msg.getRowId}")
    dbNotification.setOperationValue(msg.getJpaOperation.toString.toUpperCase)
    dbNotification
  }
}

