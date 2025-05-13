package api

import cats.data.NonEmptyList
import cats.effect.IO
import doobie.implicits._
import doobie.util.fragments
import doobie.util.transactor.Transactor

import java.sql.Timestamp

trait Metrics {
  def get(
    since: Option[Long],
    until: Option[Long],
    profileIds: Option[Seq[String]]
  ): IO[common.EventsResponse]
}

object Metrics {

  def impl(transactor: Transactor[IO]): Metrics =
    new Metrics {
      def get(
        since: Option[Long],
        until: Option[Long],
        profileIds: Option[Seq[String]]
      ): IO[common.EventsResponse] = {

        val sinceFragment = since
          .map(ms => new Timestamp(ms))
          .map(sinceTimestamp => fr"timestamp >= $sinceTimestamp")

        val untilFragment = until
          .map(ms => new Timestamp(ms))
          .map(untilTimestamp => fr"timestamp <= $untilTimestamp")

        val profileFragment =
          profileIds
            .flatMap(profileIds => NonEmptyList.fromList(profileIds.toList))
            .map(nonEmptyProfileIds => fragments.in(fr"profile_id", nonEmptyProfileIds))

        val selectorFragment =
          fragments.andOpt(sinceFragment, untilFragment, profileFragment)

        val query = selectorFragment match {
          case None           => fr"SELECT * FROM activetabs".query[common.TabEventRow].to[List]
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
                          row.timestamp.getTime,
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

    }
}
