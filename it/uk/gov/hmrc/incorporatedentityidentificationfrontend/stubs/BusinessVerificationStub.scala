
package uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.WireMockMethods

trait BusinessVerificationStub extends WireMockMethods {

  def stubCreateBusinessVerificationJourney(ctutr: String, journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping = {
    val postBody = Json.obj("journeyType" -> "BUSINESS_VERIFICATION",
      "origin" -> "vat",
      "identifiers" -> Json.obj("ctUtr" -> ctutr),
      "continueUrl" -> routes.CaptureBusinessVerificationResultController.show().url
    )
    when(method = POST, uri = s"/verification-question/journey", postBody)
      .thenReturn(
        status = status,
        body = body

      )
  }

}
