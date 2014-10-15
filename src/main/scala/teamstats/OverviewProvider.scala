package teamstats

import org.tmatesoft.svn.core.{SVNLogEntry, SVNURL}
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import scala.collection.immutable.HashSet
import scala.language.implicitConversions
import scala.collection.convert.WrapAsScala.collectionAsScalaIterable

/**
 * @author Julia Astakhova
 */
class OverviewProvider {
  def getFlatCommitOverview: Iterable[PersonContribution] = {
    val url = "http://svn.apache.org/repos/asf/spamassassin/trunk/"

    DAVRepositoryFactory.setup
    SVNRepositoryFactoryImpl.setup
    FSRepositoryFactory.setup

    val repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url))
    val startRevision = 0
    val endRevision = repository.getLatestRevision

    val logEntries = repository.log(Array(""), null, startRevision, endRevision, true, true)
    val contributions = logEntries.foldLeft(Map[String, PersonContribution]()) {(result, entry) => {
      val svnLogEntry = entry.asInstanceOf[SVNLogEntry]
      val prevContribution = result.get(svnLogEntry.getAuthor)

      val logEntryTime = svnLogEntry.getDate.getTime
      val startTime = Math.min(logEntryTime, prevContribution.map(_.startTime).getOrElse(Long.MaxValue))
      val endTime = Math.max(logEntryTime, prevContribution.map(_.endTime).getOrElse(Long.MinValue))

      result + (svnLogEntry.getAuthor -> PersonContribution(svnLogEntry.getAuthor, svnLogEntry.getAuthor, startTime, endTime))
    }}

    contributions.values
  }
}

case class PersonContribution(id: String, name: String, startTime: Long, endTime: Long)


