package de.hpi.smm

import java.io.{FileWriter, FileReader, File}
import java.util

import au.com.bytecode.opencsv.{CSVWriter, CSVReader}
import com.lambdaworks.jacks.JacksMapper
import de.hpi.smm.domain._
import de.hpi.smm.feature_extraction.FeatureBuilder
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.util.logging.RedwoodConfiguration
import scala.collection.JavaConverters._

import scala.collection.mutable


object Main {

	val FOR_ALL_POSTS = true

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader("../webapp_python/data/classification.json"))
	val postsFile = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	//	val demandCounts = new DemandCountsCounter()
	//	var brochureCounts = new BrochuresCountsCounter()
	var genericCounter = new GenericCountsCounter()

	val blacklist = Array(
		".", ",", ":", "-RRB-", "-LRB-", "$",
		// english
		"IN", "DT", "TO", "CC", "VBZ",
		// german
		"APP", "ART", "KO", "KO", "PP",
		"PR", "PT", "TRUNC", "VA", "VM", "VV"
	)

	def main(args: Array[String]): Unit = {
		println("Demand Feature Extraction")
		runDemandFeatureExtraction()

//		println("Brochure Feature Extraction")
//		runBrochureFeatureExtraction()
	}

	def runDemandFeatureExtraction(): Unit = {
		genericCounter.smoothing = false

		val features = FeatureBuilder()
			.needWords(genericCounter, "demand", (5.0, 2.0))
			.questionNumber()
			.needNGrams()
			.containsEMail()
			.addressTheReader()
			.questionWords()
			.imperativeWords()

		extractPostsLinewise { post =>
			features.touch(post)
			if (post.isClassified) {
				countTypes(post)
				countWords(post)
			}
		}()

		genericCounter.classCounts.remove("no-idea")

		val writer = new CSVWriter(new FileWriter(new File("../n2o_data/features.csv")),
			CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

		writer.writeNext(features.names)
		features.buildFeatureVector { (post, instance) =>
			val outputLine = buildLine(post, instance, "demand")
			writer.writeNext(outputLine)
		}
		writer.close()

		genericCounter.takeTopOccurrence("demand").take(10).foreach(println)
		println("----------------")
		genericCounter.takeTopNotOccurrence("demand").take(10).foreach(println)
	}

	def runBrochureFeatureExtraction(): Unit = {

		List(
			("CRM", 2.0, 4.0),
			("ECOM", 1.3, 3.7),
			("HCM", 2.3, 5.5),
			("LVM", 3.0, 5.5)
		).foreach { case (clsName, thresh1, thresh2) =>

			genericCounter = new GenericCountsCounter()
			genericCounter.smoothing = true

			val features = FeatureBuilder()
				.needWords(genericCounter, clsName, (thresh1, thresh2))
				.questionNumber()
				.needNGrams()
				.containsEMail()
				.addressTheReader()
				.questionWords()
				.imperativeWords()

			extractBrochuresLinewise { brochure =>
				features.touch(brochure)
				countTypes(brochure)
				countWords(brochure)
			}()

			val writer = new CSVWriter(new FileWriter(new File(s"../n2o_data/features_${clsName.toLowerCase}.csv")),
				CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

			writer.writeNext(features.names)
			features.buildFeatureVector { (post, instance) =>
				val outputLine = buildLine(post, instance, clsName)
				writer.writeNext(outputLine)
			}
			writer.close()


			println(s"=== $clsName ===")
			genericCounter.takeTopOccurrence(clsName, thresh1).foreach(println)
			println("======")
			genericCounter.takeTopNotOccurrence(clsName, thresh2).foreach(println)
		}

	}

	def extractPostsLinewise(extractor: Document => Unit)(count: Int = Int.MaxValue): Unit = {
		val reader = new CSVReader(new FileReader(postsFile))

		var postCount: Int = 1
		var line: Array[String] = reader.readNext()
		while (line != null && postCount <= count) {
			val id = line(0)
			val title = line(1)
			val text = line(2)

			val rawPost   = RawDocument(id, title, text, classifiedPosts.get(id).orNull)

			val isClassifiedPost = classifiedPosts.contains(id)
			if (FOR_ALL_POSTS || isClassifiedPost) {
				val sentences = detectSentences(rawPost)
				val post      = Document(id, title, text, sentences, rawPost.extractDemand())

				postCount += 1
				extractor(post)
			}
			line = reader.readNext()
		}
		reader.close()
	}

	def extractBrochuresLinewise(extractor: Document => Unit)(count: Int = Int.MaxValue): Unit = {
		val reader = new CSVReader(new FileReader(brochuresFile))

		var brochuresCount: Int = 1
		var line: Array[String] = reader.readNext()
		while (line != null && brochuresCount <= count) {
			val id = line(0)
			val text = line(1)
			val classification = line(2)
			val lang = line(4)

			val rawPost = RawDocument(id, "", text, null, lang)

			brochuresCount += 1
			val sentences = detectSentences(rawPost)
			extractor(Document(id, "", text, sentences, classification))
			line = reader.readNext()
		}
		reader.close()
	}

	def detectSentences(rawPost: RawDocument): Seq[Seq[Word]] = {
		val props = new util.Properties()
//		props.put("annotators", "tokenize,ssplit,pos,lemma,ner")
		props.put("annotators", "tokenize,ssplit,pos")
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

	private def buildLine(post: Document, instance: Array[Double], currentClass: String): Array[String] = {
		val line = new Array[String](instance.size + 2)
		line(0) = post.id
		line(line.size - 1) = if (List(currentClass, "no-idea", null).contains(post.documentClass)) post.documentClass else "no-" + currentClass
		System.arraycopy(instance.map(_.toString), 0, line, 1, instance.size)
		line
	}


	private def countTypes(doc: Document): Unit = {
		genericCounter.classCounts(doc.documentClass) += 1
	}

	private def countWords(doc: Document): Unit = {
		doc.sentences.flatten.filter { word => !blacklist.exists(word.pos.startsWith) }.map(_.text).distinct.foreach { word =>
			if (!genericCounter.wordCounts.contains(word))
				genericCounter.wordCounts(word) = new mutable.HashMap[String, Int]().withDefaultValue(0)
			genericCounter.wordCounts(word)(doc.documentClass) += 1
		}
	}

}
