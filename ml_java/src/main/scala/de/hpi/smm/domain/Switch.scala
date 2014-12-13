package de.hpi.smm.domain

case class Switch(cases: Case*) {

	def this(fun: (Document => Array[Double])) = this(Case(fun, "default"))

	def numberOfCases = cases.size

	def apply(n: Int): Case = cases(n)
	def default: Case     = apply(0)

}

object Switch {
	def apply(fun: (Document => Array[Double])) = new Switch(fun)
}


case class Case(fun: Document => Array[Double], name: String) {

	def apply(post: Document): Array[Double] = fun(post)

}
