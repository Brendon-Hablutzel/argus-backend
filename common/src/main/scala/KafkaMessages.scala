package common

import cats.syntax.all.*
import io.circe.{Encoder, Decoder}
import cats.effect.Concurrent
import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}
import io.circe.syntax._
import io.circe.generic.auto._
import cats.effect.kernel.Async

enum TabStatus(val status: String):
  case Unloaded extends TabStatus("unloaded")
  case Loading  extends TabStatus("loading")
  case Complete extends TabStatus("complete")

object TabStatus:
  implicit val tabStatusEncoder: Encoder[TabStatus] = Encoder[String].contramap(_.status)
  implicit val tabStatusDecoder: Decoder[TabStatus] = Decoder[String].map {
    case "unloaded" => TabStatus.Unloaded
    case "loading"  => TabStatus.Loading
    case "complete" => TabStatus.Complete
    case other      => throw new IllegalArgumentException(s"Unknown TabStatus: $other")
  }

final case class ActiveTab(
  timestamp: Long,
  url: String,
  title: String,
  status: TabStatus
)

object ActiveTab:
  implicit val activeTabEncoder: Encoder[ActiveTab] = deriveEncoder[ActiveTab]
  implicit val activeTabDecoder: Decoder[ActiveTab] = deriveDecoder[ActiveTab]

final case class ActiveTabMessage(
  tab: Option[ActiveTab]
)

object ActiveTabMessage:
  implicit val activeTabMessageEncoder: Encoder[ActiveTabMessage] =
    deriveEncoder[ActiveTabMessage]
  implicit val activeTabMessageDecoder: Decoder[ActiveTabMessage] =
    deriveDecoder[ActiveTabMessage]

final case class OkResponse(success: Boolean)

object OkResponse:
  implicit val okResponseEncoder: Encoder[OkResponse] = deriveEncoder[OkResponse]
  implicit val okResponseDecoder: Decoder[OkResponse] = deriveDecoder[OkResponse]
