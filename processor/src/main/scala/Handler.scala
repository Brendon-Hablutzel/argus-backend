package processor

import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import common.ActiveTabMessage
import io.circe.*
import io.circe.parser.*
import io.circe.generic.semiauto.*
import cats.Traverse.ops.toAllTraverseOps
import cats.implicits.toFlatMapOps
import org.slf4j.LoggerFactory
import cats.effect.IO
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import java.sql.Timestamp
import java.time.Instant
import doobie._
import doobie.implicits._

trait Handler:
  def handleOnce: IO[Unit]

object Handler:
  private val logger = LoggerFactory.getLogger(getClass)

  def impl(consumer: KafkaConsumer[String, String], transactor: Transactor[IO]) =
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
                                  ((record.key(), record.value(), err))
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
          _            <- IO(println(s"saving ${validRecords.length} rows: ${validRecords}"))
          res          <- if (validRecords.nonEmpty) {
                            val sql =
                              """INSERT INTO activetabs (timestamp, url, title, status, tab_selected)
                       |VALUES (?, ?, ?, ?, ?)""".stripMargin

                            val update =
                              Update[(Timestamp, String, String, String, Boolean)](sql)
                                .updateMany(
                                  validRecords
                                    .map(record =>
                                      val timestamp =
                                        Timestamp.from(Instant.ofEpochMilli(record.timestamp))
                                      record.tab match {
                                        case None      =>
                                          (
                                            timestamp,
                                            "",
                                            "",
                                            "",
                                            false
                                          )
                                        case Some(tab) =>
                                          (
                                            timestamp,
                                            tab.url,
                                            tab.title,
                                            tab.status.status,
                                            true
                                          )
                                      }
                                    )
                                )

                            update
                              .transact(transactor)
                              .map { rows =>
                                println(s"${rows} rows inserted successfully")
                              }
                              .void
                          } else IO.unit
        } yield res
    }
