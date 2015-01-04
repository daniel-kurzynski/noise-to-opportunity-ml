package de.hpi.smm.app

import org.scalatra._
import scalate.ScalateSupport

class N20Servlet extends N20DemoStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }
  
}
