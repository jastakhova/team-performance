package teamstats

import net.liftweb.json.{DefaultFormats, Serialization, NoTypeHints}
import unfiltered.request.{GET, POST, Path, Seg}
import unfiltered.response.{ResponseString, JsonContent, ComposeResponse, Ok}

import scala.util.{Failure, Success, Try}

/**
 * @author Julia Astakhova
 */
class ServerLogic extends unfiltered.filter.Plan {

  val overviewProvider = new OverviewProvider()
  val repoProvider = new RepoProvider()
  val personProvider = new PersonProvider()

  override def intent = {
    case GET(Path("/overview/flat")) => {
      import net.liftweb.json.Serialization
      implicit val formats = Serialization.formats(NoTypeHints)

      new ComposeResponse(JsonContent ~>
        ResponseString(Serialization.write(overviewProvider.getFlatCommitOverview)))
    }

    case GET(Path(Seg("user" :: Decode.utf8(name) :: "words" :: Nil))) => {
      implicit val formats = DefaultFormats
      Try {
        new ComposeResponse(JsonContent ~>
          ResponseString(Serialization.write(personProvider.getWords(name))))
      } match {
        case Success(response) => response
        case Failure(e) => {
          e.printStackTrace()
          throw new RuntimeException(e)
        }
      }
    }

    case POST(Path(Seg("repo" :: Decode.utf8(url) :: Nil))) => {
      if (repoProvider.setUrl(url))
        Ok
      else
        new unfiltered.response.Status(500)
    }
  }
}

object Server {
  def main(args: Array[String]) {
    unfiltered.jetty.Server.anylocal.
      plan(new ServerLogic).run({ svr =>
      unfiltered.util.Browser.open(svr.portBindings.head.url)
    })
  }
}

object Decode {
  import java.net.URLDecoder
  import java.nio.charset.Charset

  trait Extract {
    def charset: Charset
    def unapply(raw: String) =
      Try(URLDecoder.decode(raw, charset.name())).toOption
  }

  object utf8 extends Extract {
    val charset = Charset.forName("utf8")
  }
}

