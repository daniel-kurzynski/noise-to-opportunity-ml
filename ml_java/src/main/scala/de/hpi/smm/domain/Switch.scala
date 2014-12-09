package de.hpi.smm.domain

case class Switch(cases: Case*) {

	def this(fun: (Post => Array[Double])) = this(Case(fun, "default"))

	def numberOfCases = cases.size

	def apply(n: Int): Case = cases(n)
	def default: Case     = apply(0)

}

object Switch {
	def apply(fun: (Post => Array[Double])) = new Switch(fun)
}


case class Case(fun: Post => Array[Double], name: String) {

	def apply(post: Post): Array[Double] = fun(post)

}
