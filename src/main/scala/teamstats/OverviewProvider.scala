package teamstats

/**
 * @author Julia Astakhova
 */
class OverviewProvider {
  val dao = Dao()

  def getFlatCommitOverview: Iterable[PersonContribution] = dao.getPersonContributionsUpdatable.get
}

case class PersonContribution(id: String, name: String, startTime: Long, endTime: Long)


