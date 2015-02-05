package de.hpi.smm.classification

import java.io.{FileNotFoundException, File, FileReader}

import com.blog_intelligence.nto.DocumentExtractor
import de.hpi.smm.Constants._
import de.hpi.smm.data_reader.DataReader
import scala.collection.JavaConverters._

object ExtendedNTOClassifierBuilder {
	def build(classificationFile: File, brochuresFile: File, postsFile: File): ExtendedNTOClassifier = {

		List(classificationFile, brochuresFile, postsFile).foreach { file =>
			if (!file.exists())
				throw new FileNotFoundException(file.getAbsolutePath)
		}

		val extractor = new DocumentExtractor()

		val dataReader = new DataReader(postsFile, brochuresFile, classificationFile)
		val readingResult = extractor.readFromCSV(postsFile,brochuresFile,classificationFile)

		val extendedNTOClassifier = new ExtendedNTOClassifier(dataReader)
		extendedNTOClassifier.trainDemand(readingResult.demandDocuments)
		extendedNTOClassifier.trainProduct(readingResult.productDocuments)

		extendedNTOClassifier
	}
}
