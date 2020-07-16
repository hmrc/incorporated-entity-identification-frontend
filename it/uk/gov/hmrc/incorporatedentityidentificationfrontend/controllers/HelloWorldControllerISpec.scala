package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.test.Helpers.OK
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class HelloWorldControllerISpec extends ComponentSpecHelper {

  "GET /hello-world" should {
    "return OK" in {

      val result: WSResponse = get("/hello-world")

      result must have(httpStatus(OK))
    }
  }


}