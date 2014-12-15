package de.hpi.smm.domain

import scala.collection.mutable

class GenericCountsCounter {
  val classCounts = mutable.Map[String, Int]().withDefaultValue(0)
  val wordCounts = mutable.Map[String, mutable.Map[String, Int]]().withDefaultValue(mutable.Map[String, Int]().withDefaultValue(0))

  private def non_infinity(value: Double): Double = {
    if (value.isInfinite) 0 else value
  }

  private def calculateRatios(class_name: String): List[(String, mutable.Map[String, Int], Double, Double)] ={
    wordCounts.toList.map { case (word, currentCounts) =>
      val classProb = currentCounts(class_name).toDouble / classCounts(class_name)
      val classMissingProb = 1 - classProb

      val noClassProb = currentCounts.reduce { (acc, n) =>
        if(n._1 != class_name)
          ("", (acc._2 + n._2).toDouble / classCounts(n._1))
        else
          acc
      }._2
      val noClassMissingProb = 1 - noClassProb

      (word, currentCounts, non_infinity(classProb / noClassProb), non_infinity(classMissingProb / noClassMissingProb))
    }
  }

  def takeTopOccurrence(class_name: String) : List[(String, mutable.Map[String, Int], Double, Double)] = {

    calculateRatios(class_name).sortBy(-_._3)
  }
  def takeTopNotOccurrence(class_name: String) : List[(String, mutable.Map[String, Int], Double, Double)] = {

    calculateRatios(class_name).sortBy(-_._4)
  }
}
