package common

import doobie.util.Write
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import doobie.util.{Get, Put}

import java.sql.Timestamp

sealed trait TabStatus {
  def status: String
}

object TabStatus {
  case object Unloaded extends TabStatus { val status = "unloaded" }
  case object Loading  extends TabStatus { val status = "loading"  }
  case object Complete extends TabStatus { val status = "complete" }
  case object None     extends TabStatus { val status = ""         }

  implicit val tabStatusEncoder: Encoder[TabStatus] =
    Encoder.encodeString.contramap(_.status)
  implicit val tabStatusDecoder: Decoder[TabStatus] = Decoder.decodeString.emap {
    case "unloaded" => Right(Unloaded)
    case "loading"  => Right(Loading)
    case "complete" => Right(Complete)
    case ""         => Right(None)
    case other      => Left(s"Unknown TabStatus: $other")
  }

  implicit val tabStatusGet: Get[TabStatus] = Get[String].temap {
    case "unloaded" => Right(Unloaded)
    case "loading"  => Right(Loading)
    case "complete" => Right(Complete)
    case ""         => Right(None)
    case other      => Left(s"Unknown TabStatus: $other")
  }

  implicit val tabStatusPut: Put[TabStatus] = Put[String].contramap(_.status)

  implicit val tabStatusWrite: Write[TabStatus] = Write[TabStatus]
}

final case class ActiveTab(
  url: String,
  title: String,
  status: TabStatus,
  profileId: String
)

object ActiveTab {
  implicit val activeTabEncoder: Encoder[ActiveTab] = deriveEncoder
  implicit val activeTabDecoder: Decoder[ActiveTab] = deriveDecoder
}

final case class ActiveTabMessage(
  timestamp: Long,
  tab: ActiveTab
)

object ActiveTabMessage {
  implicit val activeTabMessageEncoder: Encoder[ActiveTabMessage] = deriveEncoder
  implicit val activeTabMessageDecoder: Decoder[ActiveTabMessage] = deriveDecoder
}

final case class TabEventRow(
  timestamp: Timestamp,
  url: String,
  title: String,
  status: TabStatus,
  profileId: String
)

final case class OkResponse(success: Boolean)

object OkResponse {
  implicit val okResponseEncoder: Encoder[OkResponse] = deriveEncoder
  implicit val okResponseDecoder: Decoder[OkResponse] = deriveDecoder
}

final case class TabEvent(
  timestamp: Long,
  url: String,
  title: String,
  status: TabStatus,
  profileId: String
)

object TabEvent {
  implicit val tabEventEncoder: Encoder[TabEvent] = deriveEncoder
}

final case class EventsResponse(events: List[TabEvent])

object EventsResponse {
//  implicit val eventsResponseEncoder: EntityEncoder[IO, EventsResponse] = jsonEncoderOf[IO, EventsResponse]
  implicit val eventsResponseEncoder: Encoder[EventsResponse] = deriveEncoder
}
