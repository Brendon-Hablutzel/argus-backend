package processor

import cats.effect.IO
import cats.implicits.toFoldableOps
import common.ActiveTabMessage
import doobie.Update
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.parser._
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory

import java.sql.Timestamp
import java.time.{Duration, Instant}
import scala.jdk.CollectionConverters.IterableHasAsScala

trait Handler {
  def handleOnce: IO[Unit]
}

object Handler {

  private val logger = LoggerFactory.getLogger(getClass)

  def impl(consumer: KafkaConsumer[String, String], transactor: Transactor[IO]): Handler =
    new Handler {
      def handleOnce: IO[Unit] =
        for {
          records      <-
            IO.blocking(consumer.poll(Duration.ofMillis(5000)))
          validRecords <- IO {
                            val mapped
                              : List[Either[(String, String, Error), ActiveTabMessage]] =
                              records.asScala.toList.map(record =>
                                decode[ActiveTabMessage](record.value()).left.map { err =>
                                  // TODO: why err.toString
                                  (
                                    (
                                      record.key(),
                                      record.value(),
                                      new Error(err.toString)
                                    )
                                  )
                                }
                              )

                            val (invalidRecords, validRecords) =
                              mapped.partitionEither(identity)

                            invalidRecords.traverse_[IO, Unit] { case (key, value, err) =>
                              IO(
                                logger.error(
                                  s"invalid record (${err}): (${key}, ${value})"
                                )
                              )
                            }

                            validRecords
                          }
          _            <- IO(logger.info(s"saving ${validRecords.length} rows: ${validRecords}"))
          res          <- if (validRecords.nonEmpty) {
                            val records = validRecords.map { record =>
                              val timestamp =
                                Timestamp.from(Instant.ofEpochMilli(record.timestamp))

                              val tab = record.tab

                              (
                                timestamp,
                                tab.url,
                                tab.title,
                                tab.status.status,
                                tab.profileId
                              )
                            }

                            val sql =
                              """INSERT INTO activetabs (timestamp, url, title, status, profile_id)
                       |VALUES (?, ?, ?, ?, ?)""".stripMargin.trim

                            val update =
                              Update[(Timestamp, String, String, String, String)](sql)

                            update
                              .updateMany(
                                records
                              )
                              .transact(transactor)
                              .map { rows =>
                                logger.info(s"${rows} rows inserted successfully")
                              }
                              .void
                          } else IO.unit
        } yield res
    }
}
