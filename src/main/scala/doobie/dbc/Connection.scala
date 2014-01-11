package doobie
package dbc

import scala.collection.JavaConverters._
import scalaz.effect.IO
import scalaz._
import Scalaz._
import java.sql
import java.sql.{ Blob, Clob, NClob, SQLXML, Struct }

/** Module of actions in the context of a `java.sql.Connection`. */
object connection extends DWorld[java.sql.Connection] {

  type Connection[+A] = Action[A]

  private[dbc] def run[A](a: Connection[A], l: Log[LogElement], s: sql.Connection): IO[A] = 
    eval(a, l, s).map(_._2)

  ////// ACTIONS, IN ALPHABETIC ORDER

  def clearWarnings: Connection[Unit] = 
    primitive(s"clearWarnings", _.clearWarnings)

  def close: Connection[Unit] =
    primitive(s"close", _.close)

  def commit: Connection[Unit] = 
    primitive(s"commit", _.commit)

  def createArrayOf(typeName: String, elements: Seq[AnyRef]): Connection[sql.Array] =
    primitive(s"createArrayOf($typeName, $elements)", _.createArrayOf(typeName, elements.toArray))

  def createBlob: Connection[Blob] = 
    primitive(s"createBlob", _.createBlob)

  def createClob: Connection[Clob] = 
    primitive(s"createClob", _.createClob)

  def createNClob: Connection[NClob] =
    primitive(s"createNClob", _.createNClob)

  def createSQLXML: Connection[SQLXML] =
    primitive(s"createSQLXML", _.createSQLXML)

  // Helper for createStatement* methods below
  private def createStatement0[A](f: sql.Connection => sql.Statement)(k: Statement[A]) = {
    import dbc.{ statement => cs }
    for {
      l <- log
      s <- primitive(s"createStatement", f)
      a <- cs.run(k, l, s).ensuring(cs.run(cs.close, l, s)).liftIO[Connection]
    } yield a
  }

  def createStatement[A]: Statement[A] => Connection[A] = 
    createStatement0(_.createStatement)

  def createStatement[A](rst: ResultSetType, rsc: ResultSetConcurrency): Statement[A] => Connection[A] = 
    createStatement0(_.createStatement(rst.toInt, rsc.toInt))

  def createStatement[A](rst: ResultSetType, rsc: ResultSetConcurrency, rsh: Holdability): Statement[A] => Connection[A] = 
    createStatement0(_.createStatement(rst.toInt, rsc.toInt, rsh.toInt))

  def createStruct(typeName: String, attributes: Array[AnyRef]): Connection[Struct] =
    primitive(s"createStruct($typeName, $attributes)", _.createStruct(typeName, attributes))

  def getAutoCommit: Connection[Boolean] = 
    primitive(s"getAutoCommit", _.getAutoCommit)

  def getCatalog: Connection[String] = 
    primitive(s"getCatalog", _.getCatalog)

  def getClientInfo: Connection[Map[String, String]] =
    primitive(s"getClientInfo", _.getClientInfo.asScala.toMap)

  def getClientInfo(name: String): Connection[String] =
    primitive(s"getClientInfo($name)", _.getClientInfo(name))

  def getHoldability: Connection[Holdability] =
    primitive(s"getHoldability", _.getHoldability).map(Holdability.unsafeFromInt)

  def getMetaData[A](k: DatabaseMetaData[A]): Connection[A] =
    for {
      l <- log
      s <- primitive(s"getMetaData", _.getMetaData)
      a <- databasemetadata.run(k, l, s).liftIO[Connection]
    } yield a

  def getTransactionIsolation: Connection[IsolationLevel] = 
    primitive(s"getTransactionIsolation", _.getTransactionIsolation).map(IsolationLevel.unsafeFromInt)

  def getTypeMap: Connection[Map[String, Class[_]]] = 
    primitive(s"getTypeMap", _.getTypeMap.asScala.toMap)

  def getWarnings: Connection[sql.SQLWarning] = 
    primitive(s"getWarnings", _.getWarnings)

  def isClosed: Connection[Boolean] =
    primitive(s"isClosed", _.isClosed)

  def isReadOnly: Connection[Boolean] = 
    primitive(s"isReadOnly", _.isReadOnly)

  def isValid(timeout: Int): Connection[Boolean] =
    primitive(s"isValid($timeout)", _.isValid(timeout))

  def nativeSQL(sql: String): Connection[String] =
    primitive(s"nativeSQL", _.nativeSQL(sql))

  // Helper for prepareCall* methods below
  private def prepareCall0[A](f: sql.Connection => sql.CallableStatement)(k: CallableStatement[A]) = {
    import dbc.{ callablestatement => cs }
    for {
      l <- log
      s <- primitive(s"prepareCall", f)
      a <- cs.run(k, l, s).ensuring(cs.run(cs.close, l, s)).liftIO[Connection]
    } yield a
  }

  def prepareCall[A](sql: String): CallableStatement[A] => Connection[A] =
    prepareCall0(_.prepareCall(sql))

  def prepareCall[A](sql: String, rst: ResultSetType, rsc: ResultSetConcurrency): CallableStatement[A] => Connection[A] =
    prepareCall0(_.prepareCall(sql, rst.toInt, rsc.toInt))

  def prepareCall[A](sql: String, rst: ResultSetType, rsc: ResultSetConcurrency, rsh: Holdability): CallableStatement[A] => Connection[A] =
    prepareCall0(_.prepareCall(sql, rst.toInt, rsc.toInt, rsh.toInt))

  // Helper for prepareStatement* methods below
  private def prepareStatement0[A](s: String, f: sql.Connection => sql.PreparedStatement)(k: PreparedStatement[A]) = {
    import dbc.{ preparedstatement => ps }
    for {
      l <- log
      s <- primitive(s, f)
      a <- push("process preparedstatement", ps.run(k, l, s).ensuring(ps.run(ps.close, l, s)).liftIO[Connection])
    } yield a
  }


  // def prepareStatement(sql: String, autoGeneratedKeys: Int): Connection[PreparedStatement] =
  //   ???

  // def prepareStatement[A](sql: String, columnIndexes: Seq[Int])(k: PreparedStatement[A]): Connection[A] =
  //    ???

  def prepareStatement[A](sql: String): PreparedStatement[A] => Connection[A] =
    prepareStatement0(s"prepareStatement($sql)", _.prepareStatement(sql))

  def prepareStatement[A](sql: String, rst: ResultSetType, rsc: ResultSetConcurrency): PreparedStatement[A] => Connection[A] =
    prepareStatement0(s"prepareStatement($sql, $rst, $rsc)", _.prepareStatement(sql, rst.toInt, rsc.toInt))

  def prepareStatement[A](sql: String, rst: ResultSetType, rsc: ResultSetConcurrency, rsh: Holdability): PreparedStatement[A] => Connection[A] =
    prepareStatement0(s"prepareStatement($sql, $rst, $rsc, $rsh)", _.prepareStatement(sql, rst.toInt, rsc.toInt, rsh.toInt))

  // def prepareStatement[A](sql: String, columnNames: Seq[String])(k: PreparedStatement[A]): Connection[A] =
  //    ???

  def releaseSavepoint(savepoint: Savepoint): Connection[Unit] =
    primitive(s"releaseSavepoint($savepoint)", _.releaseSavepoint(savepoint))

  def rollback: Connection[Unit] = 
    primitive(s"rollback", _.rollback)

  def rollback(savepoint: Savepoint): Connection[Unit] =
    primitive(s"rollback($savepoint)", _.rollback(savepoint))

  def setAutoCommit(autoCommit: Boolean): Connection[Unit] =
    primitive(s"setAutoCommit($autoCommit)", _.setAutoCommit(autoCommit))

  def setCatalog(catalog: String): Connection[Unit] =
    primitive(s"setCatalog($catalog)", _.setCatalog(catalog))

  def setClientInfo(properties: Map[String, String]): Connection[Unit] =
    primitive(s"setClientInfo($properties)", _.setClientInfo(new java.util.Properties <| (_.putAll(properties.asJava))))

  def setClientInfo(name: String, value: String): Connection[Unit] =
    primitive(s"setClientInfo($name, $value)", _.setClientInfo(name, value))

  def setHoldability(holdability: Holdability): Connection[Unit] = 
    primitive(s"setHoldability($holdability)", _.setHoldability(holdability.toInt))

  def setReadOnly(readOnly: Boolean): Connection[Unit] =
    primitive(s"setReadOnly($readOnly)", _.setReadOnly(readOnly))

  def setSavepoint: Connection[Savepoint] =
    primitive(s"setSavepoint", _.setSavepoint)

  def setSavepoint(name: String): Connection[Savepoint] =
    primitive(s"setSavepoint($name)", _.setSavepoint(name))

  def setTransactionIsolation(level: IsolationLevel): Connection[Unit] =
    primitive(s"setTransactionIsolation($level)", _.setTransactionIsolation(level.toInt))

  def setTypeMap(map: Map[String, Class[_]]): Connection[Unit] =
    primitive(s"setTypeMap($map)", _.setTypeMap(map.asJava))

}

