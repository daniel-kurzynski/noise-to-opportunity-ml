package de.hpi.smm.domain

import scala.collection.mutable

class GenericCountsCounter {
  val classCounts = mutable.Map[String, Int]().withDefaultValue(0)
  val wordCounts = mutable.Map[String, mutable.Map[String, Int]]()
  var smoothing = true

  private def non_infinity(value: Double): Double = {
    if (value.isInfinite) 0 else value
  }

  private def calculateRatios(class_name: String): List[(String, mutable.Map[String, Int], Double, Double)] ={
    wordCounts.toList.map { case (word, currentCounts) =>
      var classProb = 0.0

      if(smoothing)
        classProb = (currentCounts(class_name).toDouble + 1) / (classCounts(class_name) + 1)
      else
        classProb = currentCounts(class_name).toDouble / classCounts(class_name)

      val classMissingProb = 1 - classProb

      var noClassProb = 0.0
      var noClassMissingProb = 0.0

      // TODO: use reduce instead or so...
      classCounts.filter(p => p._1 != class_name).foreach {p =>
        var curNoClassProb = 0.0

        if(smoothing)
          curNoClassProb = (currentCounts(p._1).toDouble + 1) / (p._2 + 1)
        else
          curNoClassProb = currentCounts(p._1).toDouble / p._2

        noClassProb += curNoClassProb
        noClassMissingProb += 1 - curNoClassProb
      }

      (word, currentCounts, non_infinity(classProb / noClassProb), non_infinity(noClassMissingProb / classMissingProb))
    }
  }

  def takeTopOccurrence(class_name: String) : List[(String, mutable.Map[String, Int], Double, Double)] = {

    calculateRatios(class_name).sortBy(-_._3)
  }

  def takeTopOccurrence(class_name: String, threshold: Double) : List[(String, mutable.Map[String, Int], Double, Double)] = {

    takeTopOccurrence(class_name).takeWhile { t => t._3 > threshold }
  }

  def takeTopNotOccurrence(class_name: String) : List[(String, mutable.Map[String, Int], Double, Double)] = {

    calculateRatios(class_name).sortBy(-_._4)
  }

  def takeTopNotOccurrence(class_name: String, threshold: Double) : List[(String, mutable.Map[String, Int], Double, Double)] = {

    takeTopNotOccurrence(class_name).takeWhile { t => t._4 > threshold }
  }

}
