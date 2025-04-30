package common

import cats.syntax.all.*
import io.circe.{Encoder, Decoder}
import cats.effect.Concurrent
import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}
import io.circe.syntax._
import io.circe.generic.auto._
import cats.effect.kernel.Async
import doobie.util.Read
import doobie.util.Get
import java.sql.Timestamp
import doobie.util.Put

enum TabStatus(val status: String):
  case Unloaded extends TabStatus("unloaded")
  case Loading  extends TabStatus("loading")
  case Complete extends TabStatus("complete")
  case None     extends TabStatus("")

given Get[TabStatus] = Get[String].temap {
  case "unloaded" => Right(TabStatus.Unloaded)
  case "loading"  => Right(TabStatus.Loading)
  case "complete" => Right(TabStatus.Complete)
  case ""         => Right(TabStatus.None)
  case other      => Left(s"Unknown TabStatus: $other")
}

object TabStatus:
  implicit val tabStatusEncoder: Encoder[TabStatus] = Encoder[String].contramap(_.status)
  implicit val tabStatusDecoder: Decoder[TabStatus] = Decoder[String].map {
    case "unloaded" => TabStatus.Unloaded
    case "loading"  => TabStatus.Loading
    case "complete" => TabStatus.Complete
    case ""         => TabStatus.None
    case other      => throw new IllegalArgumentException(s"Unknown TabStatus: $other")
  }

  implicit val tabStatusGet: Get[TabStatus] = Get[String].temap {
    case "unloaded" => Right(TabStatus.Unloaded)
    case "loading"  => Right(TabStatus.Loading)
    case "complete" => Right(TabStatus.Complete)
    case ""         => Right(TabStatus.None)
    case other      => Left(s"Unknown TabStatus: $other")
  }
  implicit val tabStatusPut: Put[TabStatus] =
    Put[String].contramap(status => status.status)

final case class ActiveTab(
  url: String,
  title: String,
  status: TabStatus,
  profileId: String
)

object ActiveTab:
  implicit val activeTabEncoder: Encoder[ActiveTab] = deriveEncoder[ActiveTab]
  implicit val activeTabDecoder: Decoder[ActiveTab] = deriveDecoder[ActiveTab]

final case class ActiveTabMessage(
  timestamp: Long,
  tab: ActiveTab
)

object ActiveTabMessage:
  implicit val activeTabMessageEncoder: Encoder[ActiveTabMessage] =
    deriveEncoder[ActiveTabMessage]
  implicit val activeTabMessageDecoder: Decoder[ActiveTabMessage] =
    deriveDecoder[ActiveTabMessage]

final case class TabEventRow(
  timestamp: Timestamp,
  url: String,
  title: String,
  status: TabStatus,
  profileId: String
)

final case class OkResponse(success: Boolean)

object OkResponse:
  implicit val okResponseEncoder: Encoder[OkResponse] = deriveEncoder[OkResponse]
  implicit val okResponseDecoder: Decoder[OkResponse] = deriveDecoder[OkResponse]

final case class TabEvent(
  timestamp: Long,
  url: String,
  title: String,
  status: TabStatus,
  profileId: String
)

object TabEvent:
  implicit val tabEventEncoder: Encoder[TabEvent] = deriveEncoder[TabEvent]

final case class EventsResponse(events: List[TabEvent])

object EventsResponse:
  implicit val eventsResponseEncoder: Encoder[EventsResponse] =
    deriveEncoder[EventsResponse]
