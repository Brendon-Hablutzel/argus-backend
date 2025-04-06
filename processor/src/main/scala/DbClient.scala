package processor

import cats.effect.kernel.Resource
import cats.effect.IO
import doobie.util.transactor.Transactor

object DbClient:
  def sessionResource: Resource[IO, Transactor[IO]] =
    Resource.make {
      IO {
        Transactor.fromDriverManager[IO](
          driver = "org.postgresql.Driver",
          url = "jdbc:postgresql://localhost:5432/argus",
          user = "postgres",
          password = "postgres",
          logHandler = None
        )
      }
    } { transactor =>
      IO.unit
    }
