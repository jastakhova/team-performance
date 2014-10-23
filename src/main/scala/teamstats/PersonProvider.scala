package teamstats

/**
 * @author Julia Astakhova
 */
class PersonProvider {
  val dao = Dao()

  def getWords(user: String):List[String] = {
    val distribution = dao.getWordsUpdatable().get.filter{
      case (_, users) => {
        val userAndHisCount = users.head
        users.size == 1 && userAndHisCount._2 > 1 && userAndHisCount._1 == user
      }
    }.map{
      case (word, users) => (word, users.head._2)
    }.toList.sortBy(_._2)(Ordering[Integer].reverse)

    println(distribution)

    distribution.map(_._1)
  }
}
