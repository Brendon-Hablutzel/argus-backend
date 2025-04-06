package processor

import cats.syntax.all.*
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.Collections
import org.slf4j.LoggerFactory

object KafkaClient {
  private val logger = LoggerFactory.getLogger(getClass)

  val topic = "active-tab"

  def consumerResource[F[_]: Async]: Resource[F, KafkaConsumer[String, String]] =
    Resource.make {
      Async[F].delay {
        val props = new Properties()

        val kafkaUrl = sys.env.get("KAFKA_URL").getOrElse("localhost:9092")

        logger.info(s"kafka url: ${kafkaUrl}")

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl)

        // TODO: what should group actually be
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group")
        props.put(
          ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
          classOf[StringDeserializer]
        )
        props.put(
          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
          classOf[StringDeserializer]
        )
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        val consumer = new KafkaConsumer[String, String](props)
        consumer.subscribe(Collections.singletonList(topic))
        consumer
      }

    }(consumer => Async[F].delay(consumer.close()))
}
