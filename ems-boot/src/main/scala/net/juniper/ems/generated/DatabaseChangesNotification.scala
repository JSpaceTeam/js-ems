package net.juniper.ems.generated

import net.juniper.easyrest.notification.Notification
import spray.json._

import scala.collection.mutable

/**
 * Created by jalandip on 11/25/14.
 */
case class DatabaseChangesNotification(eventTime: String, objName: String, objType: String, rowId: Int, UUID: String, entityCategory: String, operation: String) extends Notification

object DatabaseChangesNotificationProtocol extends DefaultJsonProtocol {

  implicit object DatabaseChangeNotificationFormat extends RootJsonFormat[DatabaseChangesNotification] {

    def write(c: DatabaseChangesNotification) = {
      var fields = mutable.LinkedHashMap[String, JsValue]()
      fields += ("event-time" -> JsString(c.eventTime))
      fields += ("object-name" -> JsString(c.objName))
      fields += ("object-type" -> JsString(c.objType))
      fields += ("row-id" -> JsNumber(c.rowId))
      fields += ("uuid" -> JsString(c.UUID))
      fields += ("operation" -> JsString(c.operation))
      fields += ("entity-category" -> JsString(c.entityCategory))
      JsObject("notification" -> JsObject(fields.toMap))
    }

    def read(value: JsValue) = {
      DatabaseChangesNotification("", "", "", 0, "", "", "")
    }
  }
}