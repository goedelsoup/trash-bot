package goedelsoup.trash

import java.nio._
import java.nio.charset.Charset

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.authentication.BasicAuth
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class TwitterAlg[F[_]: Effect](
  val client: Client[F],
  token: MVar[F, String],
  creds: String
) extends Http4sDsl[F] {

  private[this] val root = Uri.uri("https://api.twitter.com")
  private[this] val logger = Slf4jLogger.unsafeCreate[F]

  def authorize: F[String] =
    client.fetch(
      Request[F](
        method = POST,
        uri = root / "oauth2" / "token" +? ("grant_type", "client_credentials")
      ).withHeaders(Header("Authorization", s"Basic $creds"))
    ) {
      case r if r.status === Ok =>
        for {
          j <- r.as[Json]
          t <- j.hcursor
            .downField("access_token")
            .as[String]
            .fold(
              e => Effect[F].raiseError(e),
              s => token.put(s).as(s)
            )
        } yield t
      case r => Effect[F].raiseError(new Throwable(s"Auth failed: ${r.status}"))
    }

  def latestForAccount(account: String): F[String] = {

    def request(token: String): F[String] =
      client.fetch(
        Request[F](
          method = GET,
          uri = root / "1.1" / "search" / "tweets.json" +? ("q", s"from:$account") +? ("result_type", "recent")
        ).withHeaders(Header("Authorization", s"Bearer $token"))
      ) {
        case r if r.status === Ok => r.as[Json].flatMap(extractTweetMedia)
        case r                    => Effect[F].raiseError(new Throwable("Could not find media in latest tweet!"))
      }

    token.tryTake >>= (_.fold(authorize)(_.pure[F])) >>= request
  }

  private[this] def extractTweetMedia(resp: Json): F[String] =
    for {
      s <- resp.hcursor.downField("statuses").as[List[Json]].map(_.head).liftTo[F]
      m <- s.hcursor.downField("entities").downField("media").as[List[Json]].liftTo[F]
      u <- m
        .map(_.hcursor.downField("url").as[String].toOption)
        .flatten
        .headOption
        .liftTo[F](new Throwable("No media found!"))
    } yield u
}
