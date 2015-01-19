package net.juniper.easyrest.yang.mapping.helper

import java.sql.{CallableStatement, PreparedStatement, ResultSet}

import com.tailf.jnc.YangType
import net.juniper.yang.mo.ietfYangTypes.Uuid
import org.apache.ibatis.`type`.{BaseTypeHandler, JdbcType}

/**
 * Created by maxin on 15-1-19.
 */
class YangTypeHandler[T <: Uuid](yangType: Class[T]) extends BaseTypeHandler[T] {
  override def setNonNullParameter(ps: PreparedStatement, i: Int, parameter: T, jdbcType: JdbcType): Unit = {
    if (jdbcType == null) {
      ps.setString(i, parameter.toString)
    }
    else {
      ps.setObject(i, parameter.toString, jdbcType.TYPE_CODE)
    }
  }

  override def getNullableResult(rs: ResultSet, columnName: String): T = {
    val s = rs.getString(columnName)
    if (s != null) {
      val constructor = yangType.getDeclaredConstructor(classOf[String]);
     constructor.newInstance(s).asInstanceOf[T]
    }
    else {
      yangType.newInstance().asInstanceOf[T]
    }
  }

  override def getNullableResult(rs: ResultSet, columnIndex: Int): T = {
    val s = rs.getString(columnIndex)
    if (s != null) {
      val constructor = yangType.getDeclaredConstructor(classOf[String]);
      constructor.newInstance(s).asInstanceOf[T]
    }
    else {
      yangType.newInstance().asInstanceOf[T]
    }
  }

  override def getNullableResult(cs: CallableStatement, columnIndex: Int): T = {
    val s = cs.getString(columnIndex)
    if (s != null) {
      val constructor = yangType.getDeclaredConstructor(classOf[String]);
      constructor.newInstance(s).asInstanceOf[T]
    }
    else {
      yangType.newInstance().asInstanceOf[T]
    }
  }
}
