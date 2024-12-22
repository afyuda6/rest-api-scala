import com.sun.net.httpserver.{HttpExchange, HttpServer}
import java.net.InetSocketAddress

import database.Sqlite
import handlers.User

object Main {
  def main(args: Array[String]): Unit = {
    val port = sys.env.getOrElse("PORT", "6010").toInt
    Sqlite.resetDatabase()
    Sqlite.initDatabase()
    val server = HttpServer.create(new InetSocketAddress(port), 0)
    server.createContext("/", (exchange: HttpExchange) => {
      User.handle(exchange);
    })
    server.setExecutor(null)
    server.start()
  }
}
