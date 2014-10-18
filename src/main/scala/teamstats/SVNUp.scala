package teamstats

import java.util.{TimerTask, Timer}

import com.mongodb.casbah.MongoCollectionBase
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import org.tmatesoft.svn.core.{SVNLogEntry, SVNURL}
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl
import org.tmatesoft.svn.core.io.{SVNRepository, SVNRepositoryFactory}
import scala.language.implicitConversions
import scala.collection.convert.WrapAsScala.collectionAsScalaIterable
import scala.util.{Success, Failure, Try}

/**
 * @author Julia Astakhova
 */
object SVNUp {

  val dao = Dao()

  def main(args: Array[String]) {
    new Timer().schedule(new TimerTask() {
      override def run(): Unit = update
    }, 0l, 5*60*1000)
  }

  private def update {
    Try {
      val basicInfoUpdatable = dao.getBasicInfoUpdatable
      val basicInfo = basicInfoUpdatable.get

      DAVRepositoryFactory.setup
      SVNRepositoryFactoryImpl.setup
      FSRepositoryFactory.setup

      val repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(basicInfo.url))
      val endRevision = repository.getLatestRevision

      if (endRevision != basicInfo.lastRevision) {
        updatePersonContributions(repository, basicInfo.lastRevision, endRevision)
        basicInfoUpdatable.update(basicInfo.copy(lastRevision = endRevision))
      }
    } match {
      case Success(nothing) => /* Cool! Nicely done! */
      case Failure(e) => e.printStackTrace()
    }
  }

  private def updatePersonContributions(repository: SVNRepository, startRevision: Long, endRevision: Long): Unit = {
    val olderPersonContributionsUpdatable = dao.getPersonContributionsUpdatable
    val olderPersonContributions = olderPersonContributionsUpdatable.get

    println("Old contribs - " + olderPersonContributions.size + " start revision - " + startRevision)
    val olderPersonContributionsMap = olderPersonContributions.map(contrib => contrib.name -> contrib).toMap

    val logEntries = repository.log(Array(""), null, startRevision, endRevision, true, true)
    val contributions = logEntries.foldLeft(olderPersonContributionsMap) {(result, entry) => {
      val svnLogEntry = entry.asInstanceOf[SVNLogEntry]
      val prevContribution = result.get(svnLogEntry.getAuthor)

      val logEntryTime = svnLogEntry.getDate.getTime
      val startTime = Math.min(logEntryTime, prevContribution.map(_.startTime).getOrElse(Long.MaxValue))
      val endTime = Math.max(logEntryTime, prevContribution.map(_.endTime).getOrElse(Long.MinValue))

      result + (svnLogEntry.getAuthor -> PersonContribution(svnLogEntry.getAuthor, svnLogEntry.getAuthor, startTime, endTime))
    }}

    println("New contribs - " + contributions.size + " endRevision - " + endRevision)
    olderPersonContributionsUpdatable.update(List() ++ contributions.values)
  }
}

case class BasicInfo(url: String = "http://svn.apache.org/repos/asf/spamassassin/trunk/", lastRevision: Long = 0)

