package goedelsoup.trash

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.dsl.Http4sDsl

class SlackAlg[F[_]: Effect](
  val client: Client[F],
  accessToken: String,
  channel: String
) extends Http4sDsl[F] {

  private[this] val root = Uri.uri("https://slack.com")

  def post(content: String): F[Unit] =
    client
      .successful(
        Request[F](
          method = POST,
          uri = root / "api" / "chat.postMessage" +? ("token", accessToken) +? ("channel", channel) +? ("text", content)
        )
      )
      .void
}
