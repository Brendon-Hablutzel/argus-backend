package processor

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {

    val resources = for {
      consumer <- KafkaClient.consumerResource
      session  <- DbClient.sessionResource
    } yield (consumer, session)

    resources.use { case (consumer, session) =>
      Handler.impl(consumer, session).handleOnce.foreverM
    }
  }
}
