package de.hpi.smm.classification

import java.io.File

import com.blog_intelligence.nto.{PredictedPost, FullPrediction, Document, NTOClassifier}
import de.hpi.smm.data_reader.DataReader
import de.hpi.smm.nlp.NLP
import weka.classifiers.Evaluation

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random

case class ClassificationOutput(prob: Double, relevantFeatures: Array[Array[Any]] = Array())
case class ExtendedClassification(cls: String, classificationOutput: ClassificationOutput)

class ExtendedNTOClassifier(val dataReader: DataReader, stopWordsFile: File, posModelFile: File) extends NTOClassifier(stopWordsFile, posModelFile) {

	def validate():Unit = {
		val demandEvaluation = demandClassifier.crossValidate()
		println("=" * 80)
		println("Demand")
		println("=" * 80)
		println(demandEvaluation.toSummaryString("", false))
		println(demandEvaluation.toMatrixString)

    val posts = mutable.ArrayBuffer[Document]()
    dataReader.readPostsLinewise { post =>
      posts += post
    }("category")

    println("=" * 80)
    println("Product")
    println("=" * 80)

    val productEvaluation = productClassifier.validate(posts)
    println(productEvaluation.toSummaryString("", false))
    println(productEvaluation.toMatrixString)

//    println("=" * 80)
//    println("Two Stages!")
//    println("=" * 80)
    //		val nto_posts = mutable.ArrayBuffer[Document]()
    //		dataReader.readPostsLinewise { post =>
    //			nto_posts += post
    //		}("nto")
    //
    //
    //		val demand_posts = mutable.ArrayBuffer[Document]()
    //		dataReader.readPostsLinewise { post =>
    //			demand_posts += post
    //		}("demand")
    //
    //		val brochures = mutable.ArrayBuffer[Document]()
    //
    //		dataReader.readBrochuresLinewise(List("en")) { brochure =>
    //			brochures += brochure
    //		}
    //
    //
    //		List(ourClassifier _, onlyProductClassifier _).zipWithIndex.foreach { case (classifier, idx) =>
    //			var accuracy = 0.0
    //			(0 to 10).foreach{ value =>
    //				val train_posts = new Random().shuffle(demand_posts).take((demand_posts.size * 0.9).toInt)
    //				val train_ids = train_posts.map(_.id)
    //				val test_posts = nto_posts.filter {doc => !train_ids.contains(doc.id)}
    //
    //				trainDemand(train_posts.asJava)
    //				if(idx == 1){
    //					val productData = brochures ++ nto_posts.filter {doc => train_ids.contains(doc.id)}
    //					trainProduct(productData.asJava)
    //				}
    //				val correctlyPredicted = test_posts.count { post =>
    //					classifier(post) == post.documentClass
    //				}
    //				accuracy += 100 * correctlyPredicted.toDouble / test_posts.size
    //			}
    //			println(accuracy / 10)
    //		}
	}

	def ourClassifier(post: Document): String = {
		val demandProb = demandClassifier.classProbability(post.wholeText)
		val productProb = productClassifier.predict(post.wholeText).maxBy(_.prob)
		if(demandProb.prob > .5) productProb.product else "None"
	}

	def onlyProductClassifier(post: Document): String = {
		productClassifier.predict(post.wholeText).maxBy(_.prob).product
	}


	def extractMostCertainPosts(num: Integer, productClass: String, posts: Seq[Document]) : List[PredictedPost] = {
		extractMostCertainPosts(num, productClass, posts.map(_.wholeText).asJava).asScala.toList
	}

	def predictDemandExtendedOutput(text: String): ExtendedClassification = {
		requireNonNull(demandClassifier)
		ExtendedClassification("demand", demandClassifier.classProbability(text))
	}
}
