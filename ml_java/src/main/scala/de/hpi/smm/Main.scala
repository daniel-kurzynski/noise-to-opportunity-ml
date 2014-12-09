package de.hpi.smm

import java.io.{FileWriter, FileReader, File}
import java.util

import au.com.bytecode.opencsv.{CSVWriter, CSVReader}
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.domain.{Word, RawPost, Post}
import de.hpi.smm.feature_extraction.{ImperativeNumberFeature, FeatureBuilder}
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.util.logging.RedwoodConfiguration
import scala.collection.JavaConverters._
import scala.collection.mutable

object Main {

	def main(args: Array[String]): Unit = {
		// TODO
		// Analyze tf-idf on both demand and no-demand
		val features = FeatureBuilder()
			.needWords()
			.questionNumber()
			.needNGrams()
			.containsEMail()
			.addressTheReader()
			.questionWords()
			.imperativeWords()

		extractPostsLinewise { post =>
			features.touch(post)
//			println(post.wholeText)
//			println(new ImperativeNumberFeature().extract(post))
		}()
		val featureFile = new File("../n2o_data/features.csv")
		val writer = new CSVWriter(new FileWriter(featureFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)
		writer.writeNext(Array("id") ++ features.names ++ Array("CATEGORY"))
		features.buildFeatureVector { (post, instance) =>
			val line = new Array[String](instance.size + 2)
			line(0) = post.id
			line(line.size - 1) = post.demandClass
			System.arraycopy(instance.map(_.toString), 0, line, 1, instance.size)
			writer.writeNext(line)
		}
		writer.close()

		println(s"Demand-Count: $demandPostNumber, No-Demand-Count: $noDemandPostNumber")
		val topWordsDemand = demandCounts.toList.map { case (word, currentCounts) =>
			val demandProb   = currentCounts.demand.toDouble / demandPostNumber
			val noDemandProb = currentCounts.noDemandCount.toDouble / noDemandPostNumber
			val relation = demandProb / noDemandProb

			val demandMissingProb   = (demandPostNumber - currentCounts.demand).toDouble / demandPostNumber
			val noDemandMissingProb = (noDemandPostNumber - currentCounts.noDemandCount).toDouble / noDemandPostNumber
			val missingRelation = noDemandMissingProb / demandMissingProb
			(word, currentCounts, if (relation.isInfinite) 0 else relation, if (missingRelation.isInfinite) 0 else missingRelation)
		}
		topWordsDemand.sortBy(-_._3).take(10).foreach(println)
		println("----------------")
		topWordsDemand.sortBy(-_._4).take(10).foreach(println)
		println("----------------")

		println(topWordsDemand.find(_._1 == "looking"))
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

			val rawPost = RawPost(id, title, text, classifiedPosts.get(id).orNull)

			if (classifiedPosts.contains(id)) {
				postCount += 1
				val sentences = detectSentences(rawPost)
				extractor(Post(id, title, text, sentences, rawPost.extractClass()))
			}
			line = reader.readNext()
		}
		reader.close()
	}

	case class DemandCounts(var demand: Int = 0, var noDemandCount: Int = 0)

	var demandCounts = mutable.Map[String, DemandCounts]()
	var demandPostNumber   = 0
	var noDemandPostNumber = 0

	def detectSentences(rawPost: RawPost): Seq[Seq[Word]] = {
		val props = new util.Properties()
//		props.put("annotators", "tokenize,ssplit,pos,lemma,ner")
		props.put("annotators", "tokenize,ssplit,pos")
//		props.put("annotators", "tokenize,ssplit")

		// shut down logging, initialize, start logging
		RedwoodConfiguration.empty().capture(System.err).apply()
		val pipeline = new StanfordCoreNLP(props)
		RedwoodConfiguration.current().clear().apply()

		val document = new Annotation(rawPost.wholeText)

		pipeline.annotate(document)

		val annotatedSentences: util.List[CoreMap] = document.get(classOf[SentencesAnnotation])

		var sentences = Vector[Vector[Word]]()

		if (rawPost.extractClass() == "demand")
			demandPostNumber += 1
		else if (rawPost.extractClass() == "no-demand")
			noDemandPostNumber += 1
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
		sentences.flatten.map(_.text).distinct.foreach { word =>
			demandCounts.get(word) match {
				case Some(currentWordCount) =>
					if (rawPost.extractClass() == "demand")
						currentWordCount.demand += 1
					else if (rawPost.extractClass() == "no-demand")
						currentWordCount.noDemandCount += 1
				case None =>
					if (rawPost.extractClass() == "demand")
						demandCounts += (word -> DemandCounts(1, 0))
					else if (rawPost.extractClass() == "no-demand")
						demandCounts += (word -> DemandCounts(0, 1))
			}
		}
		sentences
	}
}
