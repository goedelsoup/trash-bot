package goedelsoup.trash

import java.time.Instant

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import ciris._
import ciris.cats.effect._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import org.scanamo._
import org.scanamo.syntax._
import org.scanamo.auto._

import scala.concurrent.duration._
import java.time.LocalDateTime
import java.time.ZoneId

object Bot extends IOApp {

  private[this] val logger = Slf4jLogger.unsafeCreate[IO]

  val tweets = Table[MediaTweet]("critter-tweets")

  def run(args: List[String]): IO[ExitCode] =
    (for {
      c <- makeConfig
      h <- BlazeClientBuilder[IO](
        scala.concurrent.ExecutionContext.global,
        None
      ).resource.map(Logger[IO](false, false))
      m <- Resource.liftF(MVar.empty[IO, String])
      s = new SlackAlg(h, c.slackToken, c.slackChannel)
      t = new TwitterAlg[IO](h, m, c.twitterCreds)
    } yield (s, t, c)).use { resources =>
      val (slack, twitter, config) = resources
      val client: AmazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient()

      def sendIfNotLate(account: String, url: String): IO[Unit] =
        slack
          .post(url)
          .flatTap(_ => IO(Scanamo(client).exec(tweets.put(MediaTweet(url, account)))))
          .redeemWith(
            t => logger.warn("Failed to send tweet"),
            t => IO.unit
          )

      def runForAccount(account: String) =
        (for {
          _ <- logger.info(s"Checking $account")
          url <- twitter.latestForAccount(account)
          r = Scanamo(client).exec(tweets.get('tweet -> url))
          _ <- r
            .fold(sendIfNotLate(account, url))(_ => IO.unit)
        } yield ())
          .redeemWith(
            t => logger.warn("Failed to echo tweet"),
            t => logger.trace("Tweet echoed")
          )

      (fs2.Stream.eval(config.twitterAccounts.traverse(runForAccount)) ++
        fs2.Stream
          .awakeEvery[IO](1.minutes)
          .evalMap(_ => config.twitterAccounts.traverse(runForAccount))).compile.drain
        .as(ExitCode.Success)
    }

  val startTime = envF[IO, Int]("START_TIME").orValue(10)

  val endTime = envF[IO, Int]("END_TIME").orValue(19)

  val slackToken = envF[IO, String]("SLACK_TOKEN")

  val slackChannel = envF[IO, String]("SLACK_CHANNEL")

  val twitterCreds = envF[IO, String]("TWITTER_ENCODED_CREDS")

  val twitterClientSecret = envF[IO, String]("TWITTER_CLIENT_SECRET")

  val twitterAccounts = envF[IO, String]("TWITTER_ACCOUNTS")
    .mapValue(_.split(",").toList)

  def makeConfig: Resource[IO, Config] =
    Resource.liftF(
      loadConfig(startTime, endTime, slackToken, slackChannel, twitterCreds, twitterAccounts)(Config.apply).orRaiseThrowable
    )

  final case class Config(
    startTime: Int,
    endTime: Int,
    slackToken: String,
    slackChannel: String,
    twitterCreds: String,
    twitterAccounts: List[String]
  )

  final case class MediaTweet(tweet: String, account: String)
}
