package ingestor;

import cats.syntax.all.*
import io.circe.{Encoder, Decoder}
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.*
import org.http4s.Method.*
import cats.effect.Concurrent

trait Ingest[F[_]]:
  def post(activeTabMessage: Ingest.ActiveTabMessage): F[Ingest.Response]

object Ingest:
  // TODO: status should be an enum
  final case class ActiveTabMessage(
    timestamp: Long,
    url: String,
    title: String,
    status: String
  )
  object ActiveTabMessage:
    given Decoder[ActiveTabMessage]                              = Decoder.derived[ActiveTabMessage]
    given [F[_]: Concurrent]: EntityDecoder[F, ActiveTabMessage] = jsonOf

  final case class Response(success: Boolean)
  object Response:
    given Encoder[Response]                  = Encoder.AsObject.derived[Response]
    given [F[_]]: EntityEncoder[F, Response] = jsonEncoderOf

  def impl[F[_]: Concurrent]: Ingest[F] = new Ingest[F]:
    def post(activeTabMessage: Ingest.ActiveTabMessage): F[Ingest.Response] =
      Response(true).pure[F]
