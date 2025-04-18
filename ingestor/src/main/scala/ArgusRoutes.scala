package ingestor;

import cats.effect.Concurrent
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.effect.IO
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.*
import org.http4s.Method.*
import io.circe.syntax._
import common.ActiveTabMessage
import common.OkResponse
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

object ArgusRoutes:

  def ingestionRoutes[F[_]: Concurrent](I: Ingest[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] { case req @ POST -> Root / "ingest" =>
      for {
        activeTabMessage <- req.as[common.ActiveTabMessage].attempt
        resp             <- activeTabMessage match {
                              case Right(body) =>
                                for {
                                  messageReceivedResp <- I.post(body)
                                  resp                <- Ok(messageReceivedResp)
                                } yield resp
                              case Left(err)   => BadRequest(s"invalid request body: ${err.getMessage}")
                            }
      } yield resp
    }
