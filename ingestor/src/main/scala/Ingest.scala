package ingestor;

import cats.syntax.all.*
import io.circe.{Encoder, Decoder}
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.circe.*
import org.http4s.Method.*
import cats.effect.Concurrent
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import cats.effect.kernel.Async
import io.circe.syntax._

trait Ingest[F[_]]:
  def post(activeTabMessage: common.ActiveTabMessage): F[common.OkResponse]

object Ingest:

  def impl[F[_]: Async](producer: KafkaProducer[String, String]): Ingest[F] =
    new Ingest[F] {
      def post(activeTabMessage: common.ActiveTabMessage): F[common.OkResponse] =
        for {
          _        <- Async[F].delay(println(activeTabMessage))
          response <- {
            val record = new ProducerRecord[String, String](
              "active-tab",                    // topic
              // TODO: for multiple users, divide them by key perhaps
              null,                            // key
              activeTabMessage.asJson.noSpaces // value
            )

            Async[F]
              .blocking(producer.send(record).get())
              .attempt
              .map {
                case Right(_) => common.OkResponse(true)
                case Left(ex) =>
                  println(ex)
                  common.OkResponse(false)
              }
          }
        } yield response
    }
