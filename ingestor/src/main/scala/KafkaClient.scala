package ingestor

import cats.syntax.all.*
import cats.effect.{IO, Resource}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import cats.effect.kernel.Async
import org.slf4j.LoggerFactory

object KafkaClient {
  private val logger = LoggerFactory.getLogger(getClass)

  def producerResource[F[_]: Async]: Resource[F, KafkaProducer[String, String]] =
    Resource.make {
      Async[F].delay {
        val props = new Properties()

        val kafkaUrl = sys.env.get("KAFKA_URL").getOrElse("localhost:9092")

        logger.info(s"kafka url: ${kafkaUrl}")

        props.put(
          "bootstrap.servers",
          kafkaUrl
        )
        props.put("key.serializer", classOf[StringSerializer].getName)
        props.put("value.serializer", classOf[StringSerializer].getName)
        new KafkaProducer[String, String](props)
      }
    }(producer => Async[F].delay(producer.close()))
}
