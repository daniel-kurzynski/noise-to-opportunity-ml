package de.hpi.smm.app

import org.scalatra._
import scalate.ScalateSupport

class N20Servlet extends N20DemoStack {
	get("/") {
		contentType = "text/html"
		jade("hello-scalate")
	}
}
