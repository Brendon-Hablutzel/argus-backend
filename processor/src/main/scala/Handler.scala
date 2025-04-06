package processor

import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import common.ActiveTabMessage
import io.circe.*
import io.circe.parser.*
import io.circe.generic.semiauto.*
import cats.Traverse.ops.toAllTraverseOps
import cats.effect.kernel.Async
import cats.implicits.toFlatMapOps
import org.slf4j.LoggerFactory

trait Handler[F[_]]:
  def consumeOnce: F[Unit]

object Handler:
  private val logger = LoggerFactory.getLogger(getClass)

  def impl[F[_]: Async](consumer: KafkaConsumer[String, String]) =
    new Handler[F] {
      def consumeOnce: F[Unit] =
        Async[F].blocking {
          consumer.poll(Duration.ofMillis(100))
        }.flatMap { records =>
          records.asScala.toList.traverse_ { record =>
            decode[ActiveTabMessage](record.value()) match
              case Left(err)    =>
                Async[F].delay(logger.error(s"invalid record found: $err"))
              case Right(value) =>
                Async[F].delay(logger.info(s"key: ${record.key()}, value: $value"))
          }
        }
    }
