package de.hpi.smm.domain

import scala.collection.mutable

case class DemandCounts(var demand: Int = 0, var noDemandCount: Int = 0)

class DemandCountsCounter extends mutable.HashMap[String, DemandCounts] {

	val classCounts = mutable.Map[String, Int]().withDefaultValue(0)

	private def calculateRatios(): List[(String, DemandCounts, Double, Double)] = {
		toList.map { case (word, currentCounts) =>
			val demandProb   = currentCounts.demand.toDouble / classCounts("demand")
			val noDemandProb = currentCounts.noDemandCount.toDouble / classCounts("no-demand")
			val relation = demandProb / noDemandProb

			val demandMissingProb   = (classCounts("demand") - currentCounts.demand).toDouble / classCounts("demand")
			val noDemandMissingProb = (classCounts("no-demand") - currentCounts.noDemandCount).toDouble / classCounts("no-demand")
			val missingRelation = noDemandMissingProb / demandMissingProb
			(word, currentCounts, if (relation.isInfinite) 0 else relation, if (missingRelation.isInfinite) 0 else missingRelation)
		}
	}

	def takeTopOccurrence(i: Int): List[(String, DemandCounts, Double, Double)] = {
		calculateRatios().sortBy(-_._3).take(10)
	}
	def takeTopOccurrence(threshold: Double): List[(String, DemandCounts, Double, Double)] = {
		calculateRatios().sortBy(-_._3).takeWhile { t => t._3 > threshold }
	}
	def takeTopNotOccurrence(i: Int): List[(String, DemandCounts, Double, Double)] = {
		calculateRatios().sortBy(-_._4).take(10)
	}
	def takeTopNotOccurrence(threshold: Double): List[(String, DemandCounts, Double, Double)] = {
		calculateRatios().sortBy(-_._4).takeWhile { t => t._4 > threshold }
	}

}
