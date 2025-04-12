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
  def get(since: Long, profileIds: Option[Seq[String]]): IO[common.EventsResponse]

object Metrics:
  private val logger = LoggerFactory.getLogger(getClass)

  def impl(transactor: Transactor[IO]): Metrics =
    new Metrics {
      def get(since: Long, profileIds: Option[Seq[String]]): IO[common.EventsResponse] =
        val sinceTimestamp = Instant.ofEpochMilli(since)

        val sinceFragment = fr"timestamp >= $sinceTimestamp"

        val profileFragment = profileIds match {
          case Some(ids) =>
            NonEmptyList.fromList(ids.toList) match {
              case Some(nel) =>
                fragments.and(sinceFragment, fragments.in(fr"profile_id", nel))
              case None      => sinceFragment
            }
          case None      =>
            sinceFragment
        }

        val query = (fr"SELECT * FROM activetabs WHERE" ++ profileFragment)
          .query[common.TabEventRow]
          .to[List]

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
