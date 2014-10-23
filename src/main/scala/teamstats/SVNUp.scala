package teamstats

import java.util.{TimerTask, Timer}

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
  val termProcessor = new TermProcessor()

  def main(args: Array[String]) {
    new Timer().schedule(new TimerTask() {
      override def run(): Unit = update(true)
    }, 0l, 5*60*1000)
  }

  def update(reload: Boolean = false): Boolean = {
    Try {
      val basicInfoUpdatable = dao.getBasicInfoUpdatable(reload)
      val basicInfo = basicInfoUpdatable.get

      DAVRepositoryFactory.setup
      SVNRepositoryFactoryImpl.setup
      FSRepositoryFactory.setup

      val repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(basicInfo.url))
      val endRevision = repository.getLatestRevision
      println("Last revision was " + basicInfo.lastRevision + " current revision from repo is " + endRevision)

      if (endRevision != basicInfo.lastRevision) {
        val logEntries = retrieveLogEntries(repository, basicInfo.lastRevision, endRevision)
        updatePersonContributions(logEntries, reload)
        updatePersonWords(logEntries, reload)
        basicInfoUpdatable.update(basicInfo.copy(lastRevision = endRevision))
      }
    } match {
      case Success(nothing) => true /* Cool! Nicely done! */
      case Failure(e) => {
        e.printStackTrace()
        false
      }
    }
  }

  private def retrieveLogEntries(repository: SVNRepository, startRevision: Long, endRevision: Long): Iterable[SVNLogEntry] = {
    repository.log(Array(""), null, startRevision, endRevision, true, true).map(_.asInstanceOf[SVNLogEntry])
  }

  private def updatePersonContributions(logEntries: Iterable[SVNLogEntry], reload: Boolean): Unit = {
    val olderPersonContributionsUpdatable = dao.getPersonContributionsUpdatable(reload)
    val olderPersonContributions = olderPersonContributionsUpdatable.get

    println("Old contribs - " + olderPersonContributions.size)
    val olderPersonContributionsMap = olderPersonContributions.map(contrib => contrib.name -> contrib).toMap

    val contributions = logEntries.foldLeft(olderPersonContributionsMap) {(result, svnLogEntry) => {
      val prevContribution = result.get(svnLogEntry.getAuthor)

      val logEntryTime = svnLogEntry.getDate.getTime
      val startTime = Math.min(logEntryTime, prevContribution.map(_.startTime).getOrElse(Long.MaxValue))
      val endTime = Math.max(logEntryTime, prevContribution.map(_.endTime).getOrElse(Long.MinValue))

      result + (svnLogEntry.getAuthor -> PersonContribution(svnLogEntry.getAuthor, svnLogEntry.getAuthor, startTime, endTime))
    }}

    println("New contribs - " + contributions.size)
    olderPersonContributionsUpdatable.update(List() ++ contributions.values)
  }

  private def updatePersonWords(logEntries: Iterable[SVNLogEntry], reload: Boolean): Unit = {
    val wordsUpdatable = dao.getWordsUpdatable(reload)
    val olderWords = wordsUpdatable.get

    val updatedWords = logEntries.foldLeft(olderWords)((res, logEntry) => {
      val author = logEntry.getAuthor
      val message = logEntry.getMessage

      val retrievedWords = termProcessor.retrieveAllWordsAndPhrasesWithItsFrequency(message)
      retrievedWords.foldLeft(res){
        case(localres, (word, frequency)) =>
        {
          val olderWordFrequency = res.getOrElse(word, Map[String, Integer]())
          val olderWordUsage:Integer = olderWordFrequency.getOrElse(author, 0)
          res.updated(word, olderWordFrequency.updated(author, olderWordUsage + 1))
        }
      }
    })

    println("Words collected is " + updatedWords.size)
    wordsUpdatable.update(updatedWords)
  }
}

case class BasicInfo(url: String = "http://svn.apache.org/repos/asf/spamassassin/trunk/", lastRevision: Long = 0)

