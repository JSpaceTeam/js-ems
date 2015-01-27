package net.juniper.ems.notifications

import akka.actor.ActorRef
import net.juniper.easyrest.notification.EasyRestNotificationConstants.EndPoint
import net.juniper.easyrest.notification.{ Notification, EasyRestNotificationHandler, NotificationSubscriptionManagerSubSystem }

/**
 * Created by jalandip on 1/26/15.
 */
trait EmsNotifications extends NotificationSubscriptionManagerSubSystem {

  override def getNotificationHandlerActor(uri: EndPoint): ActorRef = NotificationFactory.getNotificationHandlerActor(uri)

  override def getNotificationHandler(uri: EndPoint): EasyRestNotificationHandler[_ <: Notification] = NotificationFactory.getNotificationHandler(uri)

}
