import com.sun.net.httpserver.{HttpExchange, HttpServer}
import java.net.InetSocketAddress

import database.Sqlite
import handlers.User

object Main {
  private class NotFoundHandler {
    def handle(exchange: HttpExchange): Unit = {
      val response =
        """
          |{
          |   "status": "Not Found",
          |   "code": 404
          |}
          |""".stripMargin

      exchange.getResponseHeaders.add("Content-Type", "application/json")
      exchange.sendResponseHeaders(404, response.getBytes.length)

      val outputStream = exchange.getResponseBody
      outputStream.write(response.getBytes)
      outputStream.close()
    }
  }

  def main(args: Array[String]): Unit = {
    Sqlite.resetDatabase()
    Sqlite.initDatabase()

    val server = HttpServer.create(new InetSocketAddress(6010), 0)

    val notFoundHandler = new NotFoundHandler

    server.createContext("/", (exchange: HttpExchange) => {
      val path = exchange.getRequestURI.getPath
      val query = exchange.getRequestURI.getQuery

      if ((path == "/users" || path == "/users/") && (query == null || query.isEmpty)) {
        User.handle(exchange)
      } else if (path.matches("^/users/([a-zA-Z0-9_-]+)$")) {
        User.handle(exchange)
      } else {
        notFoundHandler.handle(exchange)
      }
    })

    server.setExecutor(null)
    server.start()
    println("Server is running on http://localhost:6010")
  }
}
