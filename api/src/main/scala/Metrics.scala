package api

import cats.syntax.all.*
import io.circe.syntax._
import org.slf4j.LoggerFactory
import doobie.util.transactor.Transactor
import cats.effect.IO
import doobie._
import doobie.implicits._
import java.time.Instant
import doobie.implicits.javatimedrivernative._

trait Metrics:
  def get(since: Long): IO[common.EventsResponse]

object Metrics:
  private val logger = LoggerFactory.getLogger(getClass)

  def impl(transactor: Transactor[IO]): Metrics =
    new Metrics {
      def get(since: Long): IO[common.EventsResponse] =
        val sinceTimestamp = Instant.ofEpochMilli(since)

        for {
          res    <- sql"""select * from activetabs where timestamp >= $sinceTimestamp"""
                      .query[common.TabEventRow]
                      .to[List]
                      .transact(transactor)
          events <- IO(
                      res.map(row =>
                        common.TabEvent(
                          row.timestamp.getTime(),
                          row.url,
                          row.title,
                          row.status,
                          row.tabSelected
                        )
                      )
                    )
          ret    <- IO.pure(common.EventsResponse(events))
        } yield ret
    }
