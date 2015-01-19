package net.juniper.easyrest.yang.mapping.helper

import com.tailf.jnc.YangElement
import net.juniper.easyrest.yang.mapping.OrMappingContext
import org.apache.ibatis.reflection.MetaObject
import org.apache.ibatis.reflection.factory.ObjectFactory
import org.apache.ibatis.reflection.property.PropertyTokenizer
import org.apache.ibatis.reflection.wrapper.ObjectWrapper

/**
 * The object wrapper will help to map values to Mappingable object.
 * @param obj
 * @param metaObject
 */
class MappingObjectWrapper(sqlUnit: SqlUnitQuery, obj: YangElement, metaObject: MetaObject) extends ObjectWrapper {
  val fieldsMap = sqlUnit.fieldsMap
  val apiCtx = OrMappingContext.getApiContext
  def get(prop: PropertyTokenizer): AnyRef = {
    val v = obj.get(fieldsMap(prop.getName.toUpperCase)._mapping)
    if (v != null)
      v.asInstanceOf[AnyRef]
    else
      null
  }

  def set(prop: PropertyTokenizer, value: Any) {
    val selectPart = fieldsMap(prop.getName.toUpperCase)
    val mappingFunc = selectPart._mappingFunc
    var newValue = value
    if (mappingFunc != null)
      newValue = mappingFunc(value, obj, apiCtx)
    if (selectPart._noMapping != null)
      obj.setAttr(selectPart._noMapping, newValue.toString)
    else {
      obj.setMethodByPath(selectPart._mapping, newValue);
      //      obj.createChild(selectPart._mapping).setValue(newValue)
    }
//    println(selectPart._mapping)
//    println(obj.getChildren("uuid"))
    obj
  }

  def findProperty(name: String, useCamelCaseMapping: Boolean): String = {
    name
  }

  def getGetterNames: Array[String] = {
    throw new UnsupportedOperationException
  }

  def getSetterNames: Array[String] = {
    throw new UnsupportedOperationException
  }

  def getSetterType(name: String): Class[_] = {
    val field = fieldsMap(name.toUpperCase)
    //It's not static field in Yang Object
    println(field._mapping+"::::"+YangTypeUtil.getJavaType(obj, field._mapping))

    if (field._noMapping != null)
      classOf[Integer]
    else {
      YangTypeUtil.getJavaType(obj, field._mapping)
    }
  }

  def getGetterType(name: String): Class[_] = {
    getSetterType(name)
  }

  def hasSetter(name: String): Boolean = {
    return true
  }

  def hasGetter(name: String): Boolean = {
    return true
  }

  def instantiatePropertyValue(name: String, prop: PropertyTokenizer, objectFactory: ObjectFactory): MetaObject = {
    null
  }

  def isCollection: Boolean = {
    return false
  }

  def add(element: AnyRef) {
    throw new UnsupportedOperationException
  }

  def addAll[E](element: java.util.List[E]) {
    throw new UnsupportedOperationException
  }
}
