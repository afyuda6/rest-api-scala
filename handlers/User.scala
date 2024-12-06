package handlers

import com.sun.net.httpserver.HttpExchange
import java.sql.Connection
import scala.language.postfixOps

import database.Sqlite

object User {

  private class MethodNotAllowedHandler {
    def handle(exchange: HttpExchange): Unit = {
      val response =
        """
          |{
          |   "status": "Method Not Allowed",
          |   "code": 405
          |}
          |""".stripMargin

      exchange.getResponseHeaders.add("Content-Type", "application/json")
      exchange.sendResponseHeaders(405, response.getBytes.length)

      val outputStream = exchange.getResponseBody
      outputStream.write(response.getBytes)
      outputStream.close()
    }
  }

  def parseParams(body: String): Map[String, Option[String]] = {
    body.split("&").map { param =>
      val parts = param.split("=", 2)
      val key = parts(0)
      val value = if (parts.length > 1) Some(parts(1)) else Some("")
      key -> value
    }.toMap
  }

  def handleReadUsers(connection: Connection, exchange: HttpExchange): Unit = {
    val query = "SELECT id, name FROM users"
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(query)

    var users = List[Map[String, Any]]()
    while (resultSet.next()) {
      users = users :+ Map(
        "id" -> resultSet.getInt("id"),
        "name" -> resultSet.getString("name")
      )
    }
    resultSet.close()
    statement.close()

    val usersJson = users.map { user =>
      s"""{
         |   "id": ${user("id")},
         |   "name": "${user("name")}"
         |}""".stripMargin
    }.mkString("[", ",", "]")

    val response =
      s"""
         |{
         |   "status": "OK",
         |   "code": 200,
         |   "data": $usersJson
         |}
         |""".stripMargin

    exchange.getResponseHeaders.add("Content-Type", "application/json")
    exchange.sendResponseHeaders(200, response.getBytes.length)

    val outputStream = exchange.getResponseBody
    outputStream.write(response.getBytes)
    outputStream.close()
  }

  def handleCreateUser(connection: Connection, exchange: HttpExchange): Any = {
    val requestBody = scala.io.Source.fromInputStream(exchange.getRequestBody).mkString
    if (requestBody.isBlank) {
      val response =
        """
          |{
          |   "status": "Bad Request",
          |   "code": 400,
          |   "errors": "Missing 'name' parameter"
          |}
          |""".stripMargin

      exchange.getResponseHeaders.add("Content-Type", "application/json")
      exchange.sendResponseHeaders(400, response.getBytes.length)

      val outputStream = exchange.getResponseBody
      outputStream.write(response.getBytes)
      outputStream.close()
      return
    }

    val params = parseParams(requestBody)
    val name = params.getOrElse("name", None)
    if (name.isEmpty) {
      val response =
        """
          |{
          |   "status": "Bad Request",
          |   "code": 400,
          |   "errors": "Missing 'name' parameter"
          |}
          |""".stripMargin

      exchange.getResponseHeaders.add("Content-Type", "application/json")
      exchange.sendResponseHeaders(400, response.getBytes.length)

      val outputStream = exchange.getResponseBody
      outputStream.write(response.getBytes)
      outputStream.close()
      return
    }

    val query = "INSERT INTO users (name) VALUES (?)"
    val preparedStatement = connection.prepareStatement(query)
    val nameString = name.getOrElse("")
    preparedStatement.setString(1, nameString)
    preparedStatement.executeUpdate()
    preparedStatement.close()

    val response =
      """
        |{
        |   "status": "Created",
        |   "code": 201
        |}
        |""".stripMargin

    exchange.getResponseHeaders.add("Content-Type", "application/json")
    exchange.sendResponseHeaders(201, response.getBytes.length)

    val outputStream = exchange.getResponseBody
    outputStream.write(response.getBytes)
    outputStream.close()
  }

  def handleUpdateUser(connection: Connection, exchange: HttpExchange): Any = {
    val requestBody = scala.io.Source.fromInputStream(exchange.getRequestBody).mkString
    if (requestBody.isBlank) {
      val response =
        """
          |{
          |   "status": "Bad Request",
          |   "code": 400,
          |   "errors": "Missing 'id' or 'name' parameter"
          |}
          |""".stripMargin

      exchange.getResponseHeaders.add("Content-Type", "application/json")
      exchange.sendResponseHeaders(400, response.getBytes.length)

      val outputStream = exchange.getResponseBody
      outputStream.write(response.getBytes)
      outputStream.close()
      return
    }

    val params = parseParams(requestBody)
    val name = params.getOrElse("name", None)
    val id = params.getOrElse("id", None)
    if (name.isEmpty || id.isEmpty) {
      val response =
        """
          |{
          |   "status": "Bad Request",
          |   "code": 400,
          |   "errors": "Missing 'id' or 'name' parameter"
          |}
          |""".stripMargin

      exchange.getResponseHeaders.add("Content-Type", "application/json")
      exchange.sendResponseHeaders(400, response.getBytes.length)

      val outputStream = exchange.getResponseBody
      outputStream.write(response.getBytes)
      outputStream.close()
      return
    }

    val query = "UPDATE users SET name = ? WHERE id = ?"
    val preparedStatement = connection.prepareStatement(query)
    val nameString = name.get
    val idString = id.get
    preparedStatement.setString(1, nameString)
    preparedStatement.setString(2, idString)
    preparedStatement.executeUpdate()
    preparedStatement.close()

    val response =
      """
        |{
        |   "status": "OK",
        |   "code": 200
        |}
        |""".stripMargin

    exchange.getResponseHeaders.add("Content-Type", "application/json")
    exchange.sendResponseHeaders(200, response.getBytes.length)

    val outputStream = exchange.getResponseBody
    outputStream.write(response.getBytes)
    outputStream.close()
  }

  def handleDeleteUser(connection: Connection, exchange: HttpExchange): Any = {
    val requestBody = scala.io.Source.fromInputStream(exchange.getRequestBody).mkString
    if (requestBody.isBlank) {
      val response =
        """
          |{
          |   "status": "Bad Request",
          |   "code": 400,
          |   "errors": "Missing 'id' parameter"
          |}
          |""".stripMargin

      exchange.getResponseHeaders.add("Content-Type", "application/json")
      exchange.sendResponseHeaders(400, response.getBytes.length)

      val outputStream = exchange.getResponseBody
      outputStream.write(response.getBytes)
      outputStream.close()
      return
    }

    val params = parseParams(requestBody)
    val id = params.getOrElse("id", None)
    if (id.isEmpty) {
      val response =
        """
          |{
          |   "status": "Bad Request",
          |   "code": 400,
          |   "errors": "Missing 'id' parameter"
          |}
          |""".stripMargin

      exchange.getResponseHeaders.add("Content-Type", "application/json")
      exchange.sendResponseHeaders(400, response.getBytes.length)

      val outputStream = exchange.getResponseBody
      outputStream.write(response.getBytes)
      outputStream.close()
      return
    }

    val query = "DELETE FROM users WHERE id = ?"
    val preparedStatement = connection.prepareStatement(query)
    val idString = id.get
    preparedStatement.setString(1, idString)
    preparedStatement.executeUpdate()
    preparedStatement.close()

    val response =
      """
        |{
        |   "status": "OK",
        |   "code": 200
        |}
        |""".stripMargin

    exchange.getResponseHeaders.add("Content-Type", "application/json")
    exchange.sendResponseHeaders(200, response.getBytes.length)

    val outputStream = exchange.getResponseBody
    outputStream.write(response.getBytes)
    outputStream.close()
  }

  def handle(exchange: HttpExchange): Unit = {
    val connection: Connection = Sqlite.connect()
    val method = exchange.getRequestMethod

    method match {
      case "GET" =>
        handleReadUsers(connection, exchange)
      case "POST" =>
        handleCreateUser(connection, exchange)
      case "PUT" =>
        handleUpdateUser(connection, exchange)
      case "DELETE" =>
        handleDeleteUser(connection, exchange)
      case _ =>
        val methodNotAllowedHandler: MethodNotAllowedHandler = new MethodNotAllowedHandler
        methodNotAllowedHandler.handle(exchange)
    }

    connection.close()
  }
}
