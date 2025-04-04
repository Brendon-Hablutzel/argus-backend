package ingestor;

import cats.effect.Concurrent
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.effect.IO
import cats.FlatMap

object ArgusRoutes:

  def ingestionRoutes[F[_]: Concurrent](I: Ingest[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] { case req @ POST -> Root / "ingest" =>
      for {
        activeTabMessage <- req.as[Ingest.ActiveTabMessage].attempt
        resp             <- activeTabMessage match {
                              case Right(body) =>
                                for {
                                  _                   <- Concurrent[F].pure(println(s"received request: ${body}"))
                                  messageReceivedResp <- I.post(body)
                                  resp                <- Ok(messageReceivedResp)
                                } yield resp
                              case Left(err)   => BadRequest(s"invalid request body: ${err.getMessage}")
                            }
      } yield resp
    }
