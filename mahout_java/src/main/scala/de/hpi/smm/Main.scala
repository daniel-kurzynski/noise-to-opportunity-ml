package de.hpi.smm

import java.io.StringReader

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.{TokenStream, Analyzer}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.mahout.math.{SequentialAccessSparseVector, RandomAccessSparseVector}
import org.apache.mahout.vectorizer.encoders.{FeatureVectorEncoder, StaticWordValueEncoder}

object Main {

	def main(args: Array[String]): Unit = {
		val classification = new ClassificationMahout
		classification.classifyBrochures()
//		// io
//		val in = new StringReader("text text text to magically vectorize")
//		// Lucene tokenization
//		val analyzer: Analyzer = new StandardAnalyzer(Version.LUCENE_46)
//		val ts: TokenStream = analyzer.tokenStream("body", in)
//		val termAtt = ts.addAttribute(classOf[CharTermAttribute])
//		// Mahout
//		val encoder: FeatureVectorEncoder = new StaticWordValueEncoder("text")
//		val v1 = new RandomAccessSparseVector(100)
//		ts.reset()
//		while (ts.incrementToken()) {
//			val termBuffer = termAtt.buffer()
//			val termLen = termAtt.length()
//			val w = new String(termBuffer, 0, termLen)
//			encoder.addToVector(w, 1, v1)
//		}
//		System.out.printf("%s\n", new SequentialAccessSparseVector(v1))
	}

}
