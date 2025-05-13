package api

import cats.effect.IO
import common.EventsResponse
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object ApiRoutes {
  implicit val eventsResponseEntityEncoder: EntityEncoder[IO, EventsResponse] =
    jsonEncoderOf[IO, EventsResponse]

    def metricsRoutes(M: Metrics): HttpRoutes[IO] = {

      val dsl = new Http4sDsl[IO] {}
      import dsl._

      HttpRoutes.of[IO] { case req @ GET -> Root / "metrics" =>
        val profileIds: Option[Seq[String]] = req.multiParams.get("profileId")
        val since: Option[Long]             = req.params.get("since").map(_.toLong)
        val until: Option[Long]             = req.params.get("until").map(_.toLong)
        Ok(M.get(since, until, profileIds))
      }
    }

}
