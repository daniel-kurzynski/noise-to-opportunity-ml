package de.hpi.smm

import java.io.{FileWriter, FileReader, File}
import java.util

import au.com.bytecode.opencsv.{CSVWriter, CSVReader}
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.domain.{Word, RawPost, Post}
import de.hpi.smm.feature_extraction.FeatureBuilder
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import scala.collection.JavaConverters._

object Main {

	def main(args: Array[String]): Unit = {
		val features = FeatureBuilder()
			.questionNumber()
			.imperativeWords()
			.mostCommonWordsFromBOW()
			.needWords()
			.share()
			.thankYou()

		extractPostsLinewise { post =>
			features.touch(post)
			println(post.wholeText)

//			val vec = features.buildFeatureVector(post)
//			writer.writeNext(vec.map(_.toString))
		}(1)
		val featureFile = new File("../n2o_data/features.csv")
		val writer = new CSVWriter(new FileWriter(featureFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)
//		writer.writeNext(features.names)
		features.buildFeatureVector { (post, instance) =>
			val line = new Array[String](instance.size + 2)
			line(0) = post.id
			line(line.size - 1) = post.extractClass()
			System.arraycopy(instance.map(_.toString), 0, line, 1, instance.size)
			writer.writeNext(line)
		}
		writer.close()
	}

	def extractPostsLinewise(extractor: Post => Unit)(count: Int = Int.MaxValue): Unit = {
    val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](new FileReader("../webapp_python/data/classification.json"))
		val postsFile = new File("../n2o_data/linked_in_posts.csv")
		val reader = new CSVReader(new FileReader(postsFile))

		var postCount: Int = 1
		var line: Array[String] = reader.readNext()
		while (line != null && postCount <= count) {
			val id    = line(0)
			val title = line(1)
			val text  = line(2)

			val rawPost = RawPost(id, title, text)

			if (classifiedPosts.keySet.contains(id)) {
				postCount += 1
				val sentences = detectSentences(rawPost)
				extractor(Post(id, title, text, sentences, classifiedPosts(id)))
			}
			line = reader.readNext()
		}
		reader.close()
	}

	def detectSentences(rawPost: RawPost): Seq[Seq[Word]] = {
		val props = new util.Properties()
//		props.put("annotators", "tokenize,ssplit,pos,lemma,ner")
		props.put("annotators", "tokenize,ssplit,pos")
//		props.put("annotators", "tokenize,ssplit")
		val pipeline = new StanfordCoreNLP(props)

		val document = new Annotation(rawPost.wholeText)

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
