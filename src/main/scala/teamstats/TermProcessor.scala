package teamstats

import java.util.regex.Pattern

import com.google.common.base.Splitter
import scala.language.implicitConversions
import scala.collection.convert.WrapAsScala.iterableAsScalaIterable

/**
 * @author Julia Astakhova
 */
class TermProcessor {

  val numberPattern = """\d+""".r.pattern
  val separatorPattern = """[\p{Punct}\p{Space}]""".r.pattern
  val phraseSize = 5

  private def retrieveWords(text: String): Iterable[String] = {
    // omitEmptyString weirdly causes scalac failure (((
    Splitter.on(separatorPattern).split(text).filterNot(
      word => word.isEmpty || numberPattern.matcher(word).matches
    )
  }

  private def getSubPhrasesForAPhrase(words: List[String]): List[String] = {
    var phrase = ""
    words.reverse.foldLeft(List[String]())((res, word) => {
      val separator = if (phrase.length > 0) " " else ""
      phrase = phrase + separator + word
      phrase :: res
    })
  }

  private def getAllWordsAndPhrasesFromText(words: Iterable[String]): List[String] = {
    var phrase: List[String] = List()
    words.foldLeft(List() ++ words) { (res, word) => {
      phrase = word :: phrase
      val ret = res ++ getSubPhrasesForAPhrase(phrase)
      if (phrase.size == phraseSize)
        phrase = phrase.tail
      ret
    }}
  }

  def retrieveAllWordsAndPhrasesWithItsFrequency(text: String): Map[String, Integer] = {
    getAllWordsAndPhrasesFromText(retrieveWords(text)).foldLeft(Map[String, Integer]())((res, w) => {
      val prevCount: Integer = res.getOrElse(w, 0)
      res + (w -> (prevCount + 1))
    })
  }
}
