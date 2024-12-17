package database

import java.sql.{Connection, DriverManager}

object Sqlite {
  val url: String = "jdbc:sqlite:rest_api_scala.db"

  def connect(): Connection = {
    Class.forName("org.sqlite.JDBC")
    DriverManager.getConnection(url)
  }

  def resetDatabase(): Unit = {
    val connection = connect()
    val statement = connection.createStatement()
    val dropUserTableQuery =
      """
        |DROP TABLE IF EXISTS users;
        |""".stripMargin
    statement.executeUpdate(dropUserTableQuery)
    statement.close()
    connection.close()
  }

  def initDatabase(): Unit = {
    val connection = connect()
    val statement = connection.createStatement()
    val createUserTableQuery =
      """
        |CREATE TABLE IF NOT EXISTS users (
        |  id INTEGER PRIMARY KEY,
        |  name TEXT NOT NULL
        |);
        |""".stripMargin
    statement.executeUpdate(createUserTableQuery)
    statement.close()
    connection.close()
  }
}
