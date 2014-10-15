package teamstats

import net.liftweb.json.NoTypeHints
import unfiltered.directives.Directive
import unfiltered.filter.Plan.Intent
import unfiltered.request.{Path, Seg, GET}
import unfiltered.response.{ResponseString, JsonContent, ComposeResponse, Ok}

/**
 * @author Julia Astakhova
 */
class ServerLogic extends unfiltered.filter.Plan {

  override def intent = {
    case Path(Seg("overview" :: "flat" :: _)) => {
      import net.liftweb.json.Serialization
      implicit val formats = Serialization.formats(NoTypeHints)

      new ComposeResponse(JsonContent ~>
        ResponseString(Serialization.write(new OverviewProvider().getFlatCommitOverview)))
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

