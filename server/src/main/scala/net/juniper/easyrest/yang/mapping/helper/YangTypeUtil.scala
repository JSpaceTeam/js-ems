package net.juniper.easyrest.yang.mapping.helper

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

import com.tailf.jnc._
import net.juniper.easyrest.exception.EasyRestRuntimeException
import net.juniper.easyrest.yang.mapping.YangMappingException
import net.juniper.yang.mo.deviceManagement._
import net.juniper.yang.mo.ietfInetTypes.IpAddress
import net.juniper.yang.mo.ietfYangTypes.Uuid

import scala.collection.JavaConverters._

object YangTypeUtil {
  val tagPathYangTypeMap = new ConcurrentHashMap[(Class[_], String), String].asScala
  val tagPathJdbcTypeMap = new ConcurrentHashMap[(Class[_], String), String].asScala
  val tagPathJavaTypeMap = new ConcurrentHashMap[(Class[_], String), Class[_]].asScala

  /**
   * get default jdbc type required by mybatis. it can be overridden in mapping definition class
   * @param element
   * @param path
   * @return
   */

  def getJdbcType(element: Element, path: String): String = {
    tagPathJdbcTypeMap.get((element.getClass, path)) match {
      case Some(v) => v
      case None => {
        val yangType = tagPathYangTypeMap.get((element.getClass, path)) match {
          case Some(v) => v
          case None => SchemaTree.lookup(element.namespace, new Tagpath(element.getClass.getField("TAG_PATH").get(null) + "/" + path)).`type`
        }
        tagPathYangTypeMap((element.getClass, path)) = yangType
        val jdbcType = yangType match {
          case "int8" => "NUMERIC"
          case "int16" => "NUMERIC"
          case "int32" => "NUMERIC"
          case "int64" => "NUMERIC"
          case "uint8" => "NUMERIC"
          case "uint16" => "NUMERIC"
          case "uint32" => "NUMERIC"
          case "uint64" => "NUMERIC"
          case "decimal64" => "NUMERIC"
          case "boolean" => "NUMERIC"
          case "string" => "VARCHAR"
          case "bits" => "VARCHAR"
          case "binary" => "VARCHAR"
          case "leafref" => "VARCHAR"
          case "identityref" => "VARCHAR"
          case "empty" => "VARCHAR"
          case "yang:uuid" => "VARCHAR"
          case _ => throw new EasyRestRuntimeException("Unsupported yang type:" + yangType)
        }
        tagPathJdbcTypeMap((element.getClass, path)) = jdbcType
        jdbcType
      }
    }
  }

  def getJavaType[T <: Element](element: T, path: String): Class[_] = {
    tagPathJavaTypeMap.get((element.getClass, path)) match {
      case Some(v) => v
      case None => {
        val yangType = tagPathYangTypeMap.get((element.getClass, path)) match {
          case Some(v) => v
          case None => {
            val tagPath = new Tagpath(element.getClass.getField("TAG_PATH").get(null) + "/" + path)
            val attr = SchemaTree.lookup(element.namespace, tagPath)
            if (attr == null)
              throw new YangMappingException("Can't find attribute by path. Namespace: " + element.namespace + ", path:" + tagPath.toString)
            attr.`type`
          }
        }
        tagPathYangTypeMap((element.getClass, path)) = yangType
        val javaType = yangType match {
          case "int8" => classOf[Byte]
          case "int16" => classOf[Short]
          case "int32" => classOf[Integer]
          case "int64" => classOf[Long]
          case "uint8" => classOf[Short]
          case "uint16" => classOf[Integer]
          case "uint32" => classOf[Long]
          case "uint64" => classOf[BigInteger]
          case "decimal64" => classOf[Double]
          case "boolean" => classOf[Boolean]
          case "string" => classOf[String]
          case "empty" => classOf[Boolean]
          case "yang:uuid" => classOf[Uuid]

          case "connectionTypeEnum" => classOf[ConnectionTypeEnum]
          case "connectionStatusEnum" => classOf[ConnectionStatusEnum]
          case "inet:ip-address" => classOf[IpAddress]
          case "authEnum" => classOf[AuthEnum]
          case "redundancyStatusEnum" => classOf[RedundancyStatusEnum]
          case "deviceConfigStatusEnum" => classOf[DeviceConfigStatusEnum]
          case "candidateConfigStateEnum" => classOf[CandidateConfigStateEnum]
          case _ => throw new EasyRestRuntimeException("Unsupported yang type: " + yangType)
        }
        tagPathJavaTypeMap((element.getClass, path)) = javaType
        javaType
      }
    }
  }
}
