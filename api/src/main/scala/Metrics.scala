package api

import cats.syntax.all.*
import io.circe.syntax._
import org.slf4j.LoggerFactory
import doobie.util.transactor.Transactor
import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.fragments
import java.time.Instant
import doobie.implicits.javatimedrivernative._
import cats.data.NonEmptyList

trait Metrics:
  def get(
    since: Option[Long],
    until: Option[Long],
    profileIds: Option[Seq[String]]
  ): IO[common.EventsResponse]

object Metrics:
  private val logger = LoggerFactory.getLogger(getClass)

  def impl(transactor: Transactor[IO]): Metrics =
    new Metrics {
      def get(
        since: Option[Long],
        until: Option[Long],
        profileIds: Option[Seq[String]]
      ): IO[common.EventsResponse] =

        val sinceFragment = since
          .map(Instant.ofEpochMilli(_))
          .map(sinceTimestamp => fr"timestamp >= $sinceTimestamp")

        val untilFragment = until
          .map(Instant.ofEpochMilli(_))
          .map(untilTimestamp => fr"timestamp <= $untilTimestamp")

        val profileFragment =
          profileIds
            .map(profileIds => NonEmptyList.fromList(profileIds.toList))
            .flatten
            .map(nonEmptyProfileIds => fragments.in(fr"profile_id", nonEmptyProfileIds))

        val selectorFragment =
          fragments.andOpt(sinceFragment, untilFragment, profileFragment)

        val query = selectorFragment match {
          case None           => (fr"SELECT * FROM activetabs").query[common.TabEventRow].to[List]
          case Some(selector) =>
            (fr"SELECT * FROM activetabs WHERE" ++ selector)
              .query[common.TabEventRow]
              .to[List]
        }

        for {
          res    <- query.transact(transactor)
          events <- IO(
                      res.map(row =>
                        common.TabEvent(
                          row.timestamp.getTime(),
                          row.url,
                          row.title,
                          row.status,
                          row.profileId
                        )
                      )
                    )
          ret    <- IO.pure(common.EventsResponse(events))
        } yield ret
    }
