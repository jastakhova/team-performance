package teamstats

/**
 * @author Julia Astakhova
 */
class RepoProvider {

  val dao = Dao()

  def setUrl(url: String): Boolean = {
    val basicInfoUpdatable = dao.getBasicInfoUpdatable
    val basicInfo = basicInfoUpdatable.get
    if (basicInfo.url != url) {
      val personContribUpdatable = dao.getPersonContributionsUpdatable
      personContribUpdatable.update(List())
      basicInfoUpdatable.update(BasicInfo(url))
      return SVNUp.update
    }
    true
  }
}
