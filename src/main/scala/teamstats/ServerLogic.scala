package teamstats

import net.liftweb.json.NoTypeHints
import unfiltered.request.{GET, POST, Path, Seg}
import unfiltered.response.{ResponseString, JsonContent, ComposeResponse, Ok}

import scala.util.Try

/**
 * @author Julia Astakhova
 */
class ServerLogic extends unfiltered.filter.Plan {

  val overviewProvider = new OverviewProvider()
  val repoProvider = new RepoProvider()

  override def intent = {
    case GET(Path("/overview/flat")) => {
      import net.liftweb.json.Serialization
      implicit val formats = Serialization.formats(NoTypeHints)

      new ComposeResponse(JsonContent ~>
        ResponseString(Serialization.write(overviewProvider.getFlatCommitOverview)))
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

