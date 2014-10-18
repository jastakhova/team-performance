package teamstats

import com.mongodb.casbah.MongoCollectionBase
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._


/**
 * @author Julia Astakhova
 */
class Dao {

  val db = MongoClient("localhost", 27017)("main")

  def getBasicInfoUpdatable: Updatable[BasicInfo] = {
    val basicInfoCollection = db("basic_info")
    val basicInfoDBO = basicInfoCollection.findOne()
    val basicInfo = basicInfoDBO.map(dbo => grater[BasicInfo].asObject(dbo)).getOrElse(BasicInfo())
    new Updatable[BasicInfo](basicInfoCollection, basicInfoDBO, basicInfo, this)
  }

  def getPersonContributionsUpdatable: Updatable[List[PersonContribution]] = {
    val personContributionsCollection = db("person_contribution")
    val contribField = "contribs"

    val olderPersonContributionsDBO = personContributionsCollection.findOne()
    val olderPersonContributions = olderPersonContributionsDBO.map(
      dbo =>
      {
        val contribListOption = dbo.as[Option[BasicDBList]](contribField)
        val contribList = contribListOption.map(l => l.toList.map(_.asInstanceOf[DBObject])).getOrElse(List())
        contribList.map(dbo2 => grater[PersonContribution].asObject(dbo2))
      }
    ).getOrElse(List())

    new Updatable[List[PersonContribution]](
      personContributionsCollection, olderPersonContributionsDBO, olderPersonContributions, this,
      new ObjectContructor[List[PersonContribution]] {
        override def construct(data: List[PersonContribution])(implicit m: Manifest[List[PersonContribution]]): DBObject = {
          val contribDBOs = data.map(contrib => grater[PersonContribution].asDBObject(contrib))
          DBObject(contribField -> MongoDBList(contribDBOs: _*))
        }
      })
  }

  def getPersonContributions: List[PersonContribution] = getPersonContributionsUpdatable.get

  def update(collection: MongoCollectionBase, oldDBO: Option[DBObject], newDBO: DBObject): Unit = {
    val idFieldName = "_id"
    val query = DBObject(idFieldName -> oldDBO.map(_.get(idFieldName)).getOrElse(""))
    collection.update(query, newDBO, upsert = true)
  }
}

object Dao {
  def apply() = new Dao()
}

class Updatable[T <: scala.AnyRef](collection:MongoCollectionBase, dbo: Option[DBObject], obj:T, dao: Dao,
                   contructor: ObjectContructor[T] = new PrimitiveContructor[T]()) {
  def get = obj

  def update(data:T)(implicit m: Manifest[T]): Unit = {
    dao.update(collection, dbo, contructor.construct(data))
  }
}

trait ObjectContructor[T] {
  def construct(data: T)(implicit m: Manifest[T]): DBObject
}

class PrimitiveContructor[T <: scala.AnyRef] extends ObjectContructor[T] {
  override def construct(data: T)(implicit m: Manifest[T]): DBObject = grater[T].asDBObject(data)
}