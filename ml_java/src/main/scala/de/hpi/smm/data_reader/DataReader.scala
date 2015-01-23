package de.hpi.smm.data_reader

import java.io.{File, FileReader}
import java.util

import au.com.bytecode.opencsv.CSVReader
import com.blog_intelligence.nto.{RawDocument, Document}
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.domain.Word
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.util.logging.RedwoodConfiguration
import scala.collection.JavaConverters._

/**
 * Created by Daniel on 06.01.2015.
 */
class DataReader(val classifiedPosts :Map[String, Map[String, Map[String, String]]],
								 	val postsFile :File,
									val brochuresFile :File,
									val FOR_ALL_POSTS :Boolean) {

	def readBrochuresLinewise(languages: List[String] = List("de", "en"))(extractor: Document => Unit): Unit = {
		val reader = new CSVReader(new FileReader(brochuresFile))

		var brochuresCount: Int = 1
		var line: Array[String] = reader.readNext()
		var lastID = "-1";
		while (line != null && brochuresCount <= Int.MaxValue) {
			if(line.length<4)
				println("!"+line(0)+", lastID: "+lastID)
			val id = line(0)
			lastID = id;
			val text = line(1)
			val classification = line(2)
			val language = line(4)

			if (languages.contains(language)) {
				val rawPost = RawDocument(id, "", text, null, language)

				brochuresCount += 1
				val sentences = detectSentences(rawPost)
				extractor(Document(id, "", text, sentences, classification))
			}
			line = reader.readNext()
		}
		reader.close()
	}

	def readPostsLinewise(extractor: Document => Unit)(className: String = "demand", count: Int = Int.MaxValue): Unit = {
		val reader = new CSVReader(new FileReader(postsFile))

		var postCount: Int = 1
		var line: Array[String] = reader.readNext()
		while (line != null && postCount <= count) {
			val id = line(0)
			val title = line(1)
			val text = line(2)

			val rawPost = RawDocument(id, title, text, classifiedPosts.get(id).orNull)

			val isClassifiedPost = classifiedPosts.contains(id)
			if (isClassifiedPost) {
				val sentences = detectSentences(rawPost)
				val post = Document(id, title, text, sentences, rawPost.extract(className))

				postCount += 1
				extractor(post)
			}
			line = reader.readNext()
		}
		reader.close()
	}

	def detectSentences(rawPost: RawDocument): Seq[Seq[Word]] = {
		val props = new util.Properties()
//		props.put("annotators", "tokenize,ssplit,pos,lemma,ner")
		props.put("annotators", "tokenize,ssplit,pos,lemma")
		if (rawPost.lang == "de") {
//			println("Using german")
			props.put("pos.model", "../n2o_data/german-fast.tagger")
		}
		else if (rawPost.lang == "en" || rawPost.lang == null) Unit
		else throw new RuntimeException("Unknown language.")

//		props.put("annotators", "tokenize,ssplit")

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
				val pos = token.get(classOf[PartOfSpeechAnnotation])
				val ner = token.get(classOf[NamedEntityTagAnnotation])
				currentSentence :+= Word(word, pos, ner)
			}
			sentences :+= currentSentence
		}

		sentences
	}
}
