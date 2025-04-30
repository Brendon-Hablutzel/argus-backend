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
          url =
            sys.env.getOrElse("DATABASE_URL", "jdbc:postgresql://localhost:5432/argus"),
          // url = "jdbc:postgresql://localhost:5432/argus",
          // url = "jdbc:postgresql://host.docker.internal:5432/argus"
          user = sys.env.getOrElse("DATABASE_USERNAME", "postgres"),
          password = sys.env.getOrElse("DATABASE_PASSWORD", "postgres"),
          logHandler = None
        )
      }
    } { transactor =>
      IO.unit
    }
