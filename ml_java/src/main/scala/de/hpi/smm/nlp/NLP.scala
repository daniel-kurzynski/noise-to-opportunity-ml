package de.hpi.smm.nlp

import java.io.File

import com.blog_intelligence.nto.RawDocument
import de.hpi.smm.domain.Word
import java.util
import scala.collection.JavaConverters._

import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.util.logging.RedwoodConfiguration

import scala.io.Source

object NLP {

	val stopWords = Source.fromFile(new File("../n2o_data/stopwords.txt")).getLines().toSet

	def detectSentences(rawPost: RawDocument): Seq[Seq[Word]] = {
		val props = new util.Properties()
//		props.put("annotators", "tokenize,ssplit,pos,lemma,ner")
		props.put("annotators", "tokenize,ssplit,pos")
		if (rawPost.lang == "de") {
			props.put("pos.model", "../n2o_data/german-fast.tagger")
		}
		else if (rawPost.lang == "en" || rawPost.lang == null) Unit
		else throw new RuntimeException("Unknown language.")

		// shut down logging, initialize, start logging
		RedwoodConfiguration.empty().capture(System.err).apply()
		val pipeline = new StanfordCoreNLP(props)
		RedwoodConfiguration.current().clear().apply()

		val document = new Annotation(rawPost.wholeText.toLowerCase)
		pipeline.annotate(document)
		val annotatedSentences: util.List[CoreMap] = document.get(classOf[SentencesAnnotation])

		var sentences = Vector[Vector[Word]]()

		annotatedSentences.asScala.foreach { sentence =>
			var currentSentence = Vector[Word]()
			sentence.get(classOf[TokensAnnotation]).asScala.foreach { token =>
				val word = token.get(classOf[TextAnnotation])
				val tokenizedWord = TokenizerHelper.tokenize(word, true)
				if (tokenizedWord.nonEmpty && !stopWords.contains(word)) {
					val word = tokenizedWord(0)
					val pos = token.get(classOf[PartOfSpeechAnnotation])
					val ner = token.get(classOf[NamedEntityTagAnnotation])
					currentSentence :+= Word(word, pos, ner)
				}
			}
			sentences :+= currentSentence
		}

		sentences
	}
}
