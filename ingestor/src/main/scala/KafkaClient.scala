package ingestor

import cats.syntax.all.*
import cats.effect.{IO, Resource}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import cats.effect.kernel.Async
import org.slf4j.LoggerFactory
import org.apache.kafka.clients.producer.ProducerConfig

object KafkaClient {
  private val logger = LoggerFactory.getLogger(getClass)

  def producerResource[F[_]: Async]: Resource[F, KafkaProducer[String, String]] =
    Resource.make {
      Async[F].delay {
        val props = new Properties()

        val kafkaUrl = sys.env.get("KAFKA_URL").getOrElse("localhost:9092")

        logger.info(s"kafka url: ${kafkaUrl}")

        props.put(
          ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
          kafkaUrl
        )
        props.put(
          ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
          classOf[StringSerializer].getName
        )
        props.put(
          ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
          classOf[StringSerializer].getName
        )
        new KafkaProducer[String, String](props)
      }
    }(producer => Async[F].delay(producer.close()))
}
