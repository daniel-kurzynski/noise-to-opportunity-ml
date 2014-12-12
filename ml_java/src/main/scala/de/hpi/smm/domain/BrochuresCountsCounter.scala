package de.hpi.smm.domain

import scala.collection.mutable

case class BrochureCounts(var crm: Int = 0, var ecom: Int = 0, var hcm: Int = 0, var lvm: Int = 0)

class BrochuresCountsCounter extends mutable.HashMap[Word, BrochureCounts] {
	val classCounts = mutable.Map[String, Int]().withDefaultValue(0)

	def overallBrochures: Int = classCounts.values.sum

  private def non_infinity(value: Double): Double = {
    if (value.isInfinite) 0 else value
  }

	private def calculateRatios(): List[(Word, BrochureCounts, Double, Double, Double, Double)] = {
		toList.map { case (word, currentCounts) =>
			val crmProb  = currentCounts.crm.toDouble  / classCounts("CRM")
			val ecomProb = currentCounts.ecom.toDouble / classCounts("ECOM")
			val hcmProb  = currentCounts.hcm.toDouble  / classCounts("HCM")
			val lvmProb  = currentCounts.lvm.toDouble  / classCounts("LVM")


      val crmRelation  = crmProb  / (ecomProb + hcmProb + lvmProb)
      val ecomRelation = ecomProb / (crmProb + hcmProb + lvmProb)
      val hcmRelation  = hcmProb  / (ecomProb + crmProb + lvmProb)
      val lvmRelation  = lvmProb  / (ecomProb + hcmProb + crmProb)

			(word, currentCounts, non_infinity(crmRelation), non_infinity(ecomRelation), non_infinity(hcmRelation), non_infinity(lvmRelation))

//			val relation = crmProb / noDemandProb
//
//			val demandMissingProb   = (classCounts("demand") - currentCounts.demand).toDouble / classCounts("demand")
//			val noDemandMissingProb = (classCounts("no-demand") - currentCounts.noDemandCount).toDouble / classCounts("no-demand")
//			val missingRelation = noDemandMissingProb / demandMissingProb
//			(word, currentCounts, if (relation.isInfinite) 0 else relation, if (missingRelation.isInfinite) 0 else missingRelation)
		}
	}

	def takeCRM(i: Int): List[(Word, BrochureCounts, Double, Double, Double, Double)] = {
		calculateRatios().sortBy(-_._3).take(i)
	}
	def takeECOM(i: Int): List[(Word, BrochureCounts, Double, Double, Double, Double)] = {
		calculateRatios().sortBy(-_._4).take(i)
	}
	def takeHCM(i: Int): List[(Word, BrochureCounts, Double, Double, Double, Double)] = {
		calculateRatios().sortBy(-_._5).take(i)
	}
	def takeLVM(i: Int): List[(Word, BrochureCounts, Double, Double, Double, Double)] = {
		calculateRatios().sortBy(-_._6).take(i)
	}
}
