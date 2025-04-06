package processor

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    KafkaClient.consumerResource[IO].use(Handler.impl[IO](_).consumeOnce.foreverM)
