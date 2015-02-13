package de.hpi.smm.classification

import java.io.{FileNotFoundException, File, FileReader}

import com.blog_intelligence.nto.DocumentExtractor
import de.hpi.smm.Constants._
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.nlp.NLP
import scala.collection.JavaConverters._

object ExtendedNTOClassifierBuilder {
	def build(classificationFile: File, brochuresFile: File, postsFile: File, stopWordsFile: File, posModelFile: File): ExtendedNTOClassifier = {

		List(classificationFile, brochuresFile, postsFile).foreach { file =>
			if (!file.exists())
				throw new FileNotFoundException(file.getAbsolutePath)
		}

		val extractor = new DocumentExtractor(stopWordsFile, posModelFile)
		val nlp = new NLP(stopWordsFile, posModelFile)
		val dataReader = new DataReader(postsFile, brochuresFile, classificationFile, nlp)
		val readingResult = extractor.readFromCSV(postsFile, brochuresFile, classificationFile)

		val extendedNTOClassifier = new ExtendedNTOClassifier(dataReader, stopWordsFile, posModelFile)
		extendedNTOClassifier.trainDemand(readingResult.demandDocuments)
		extendedNTOClassifier.trainProduct(readingResult.productDocuments)

		extendedNTOClassifier
	}
}
