package teamstats

import com.mongodb.casbah.{Imports, MongoCollectionBase}
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import scala.language.implicitConversions
import scala.collection.convert.WrapAsScala.mapAsScalaMap


/**
 * @author Julia Astakhova
 */
class Dao {

  val db = MongoClient("localhost", 27017)("main")

  def getBasicInfoUpdatable(cleanPrevious: Boolean = false): Updatable[BasicInfo] = {
    val basicInfoCollection = retrieveCollection("basic_info", cleanPrevious)
    val basicInfoDBO = basicInfoCollection.findOne()
    val basicInfo = basicInfoDBO.map(dbo => grater[BasicInfo].asObject(dbo)).getOrElse(BasicInfo())
    new Updatable[BasicInfo](basicInfoCollection, basicInfoDBO, basicInfo, this)
  }

  def getPersonContributionsUpdatable(cleanPrevious: Boolean = false): Updatable[List[PersonContribution]] = {
    val personContributionsCollection = retrieveCollection("person_contribution", cleanPrevious)
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

  def getWordsUpdatable(cleanPrevious: Boolean = false): Updatable[Map[String, Map[String, Integer]]] = {
    val wordsCollection = retrieveCollection("words", cleanPrevious)
    val olderWordsDBO = wordsCollection.findOne()

    val words: Map[String, Map[String, Integer]] = olderWordsDBO.map(
     dbo =>
     {
       dbo.toMap.flatMap {
         case (key, value) if value.isInstanceOf[DBObject] =>
           Some(key.toString, value.asInstanceOf[DBObject].toMap.map {
             case (key, value) => (key.toString, value.asInstanceOf[Integer])
           }.toMap)
         case _ => None
       }.toMap
     }
    ).getOrElse(Map[String, Map[String, Integer]]())

    new Updatable[Map[String, Map[String, Integer]]] (wordsCollection, olderWordsDBO, words, this,
      new ObjectContructor[Map[String, Map[String, Integer]]]() {
        override def construct(data: Map[String, Map[String, Integer]])(implicit m: Manifest[Map[String, Map[String, Integer]]]): Imports.DBObject = {
          new MongoDBObject(data.map{
            case (key, value) => (key, new MongoDBObject(value))
          })
        }
      })
  }

  private def retrieveCollection(name: String, clean: Boolean): MongoCollection = {
    val collection = db(name)
    if (clean)
      collection.drop()
    collection
  }

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