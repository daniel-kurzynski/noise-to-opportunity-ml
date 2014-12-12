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

object Main {

	val classifiedPosts = JacksMapper.readValue[Map[String, Map[String, Map[String, String]]]](
		new FileReader("../webapp_python/data/classification.json"))
	val postsFile     = new File("../n2o_data/linked_in_posts.csv")
	val brochuresFile = new File("../n2o_data/brochures.csv")

	val demandCounts = new DemandCountsCounter()
	var brochureCounts = new BrochuresCountsCounter()

  val blacklist = Array(
    ".", ",", ":", "-RRB-", "-LRB-", "$",
    // english
    "IN", "DT", "TO", "CC", "VBZ",
    // german
    "APP","ART","KO","KO","PP",
    "PR","PT","TRUNC","VA","VM","VV"
  )

	def main(args: Array[String]): Unit = {
//		runDemandFeatureExtraction()

		runBrochureFeatureExtraction()
	}

	def runBrochureFeatureExtraction(): Unit = {
		extractBrochuresLinewise { brochure =>
			countProductTypes(brochure)
			countProductWords(brochure)
		}()

    println("=== CRM ===")
    brochureCounts.takeCRM(10).foreach(println)
    println("======")
    brochureCounts.takeNonCRM(10).foreach(println)
    println("=== ECOM ===")
    brochureCounts.takeECOM(10).foreach(println)
    println("======")
    brochureCounts.takeNonECOM(10).foreach(println)
    println("=== HCM ===")
    brochureCounts.takeHCM(10).foreach(println)
    println("======")
    brochureCounts.takeNonHCM(10).foreach(println)
    println("=== LVM ===")
    brochureCounts.takeLVM(10).foreach(println)
    println("======")
    brochureCounts.takeNonLVM(10).foreach(println)
	}

	def runDemandFeatureExtraction(): Unit = {
		val features = FeatureBuilder()
			.needWords(demandCounts)
			.questionNumber()
			.needNGrams()
			.containsEMail()
			.addressTheReader()
			.questionWords()
			.imperativeWords()

		extractPostsLinewise { post =>
			features.touch(post)
			countDemandTypes(post)
			countDemandWords(post)
		}()
		val writer = new CSVWriter(new FileWriter(new File("../n2o_data/features.csv")),
			CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER)

		writer.writeNext(features.names)
		features.buildFeatureVector { (post, instance) =>
			val outputLine = buildLine(post, instance)
			writer.writeNext(outputLine)
		}
		writer.close()

		demandCounts.takeTopOccurrence(10).foreach(println)
		println("----------------")
		demandCounts.takeTopNotOccurrence(10).foreach(println)
	}

	def extractBrochuresLinewise(extractor: Document => Unit)(count: Int = Int.MaxValue): Unit = {
		val reader = new CSVReader(new FileReader(brochuresFile))

		var brochuresCount: Int = 1
		var line: Array[String] = reader.readNext()
		while (line != null && brochuresCount <= count) {
			val id    = line(0)
			val text  = line(1)
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

	def extractPostsLinewise(extractor: Document => Unit)(count: Int = Int.MaxValue): Unit = {
		val reader = new CSVReader(new FileReader(postsFile))

		var postCount: Int = 1
		var line: Array[String] = reader.readNext()
		while (line != null && postCount <= count) {
			val id    = line(0)
			val title = line(1)
			val text  = line(2)

			val rawPost = RawDocument(id, title, text, classifiedPosts.get(id).orNull)

			if (classifiedPosts.contains(id)) {
				postCount += 1
				val sentences = detectSentences(rawPost)
				extractor(Document(id, title, text, sentences, rawPost.extractDemand()))
			}
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

	private def buildLine(post: Document, instance: Array[Double]): Array[String] = {
		val line = new Array[String](instance.size + 2)
		line(0) = post.id
		line(line.size - 1) = post.documentClass
		System.arraycopy(instance.map(_.toString), 0, line, 1, instance.size)
		line
	}

	private def countDemandTypes(post: Document): Unit = {
		demandCounts.classCounts(post.documentClass) += 1
	}
	private def countProductTypes(brochures: Document): Unit = {
		brochureCounts.classCounts(brochures.documentClass) += 1
	}

	private def countDemandWords(post: Document): Unit = {
		post.textTokens.distinct.foreach { word =>
			if (!demandCounts.contains(word))
				demandCounts(word) = DemandCounts()

			if (post.documentClass == "demand")
				demandCounts(word).demand += 1
			else if (post.documentClass == "no-demand")
				demandCounts(word).noDemandCount += 1
		}
	}
	private def countProductWords(brochure: Document): Unit = {
		brochure.sentences.flatten.filter { word => !blacklist.exists(word.pos.startsWith) }.map(_.text).distinct.foreach { word =>
			if (!brochureCounts.contains(word))
				brochureCounts(word) = BrochureCounts()

			brochure.documentClass match {
				case "CRM" =>
					brochureCounts(word).crm += 1
				case "ECOM" =>
					brochureCounts(word).ecom += 1
				case "HCM" =>
					brochureCounts(word).hcm += 1
				case "LVM" =>
					brochureCounts(word).lvm += 1
				case s => throw new RuntimeException(s"Unknown class ${s.toString}")
			}
		}
	}
}
