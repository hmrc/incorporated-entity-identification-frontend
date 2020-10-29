
package uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.Json
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.RegistrationStatus
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.{WireMockMethods, WiremockHelper}

trait RegisterStub extends WireMockMethods {

  def stubRegister(crn: String, ctutr: String)(status: Int, body: RegistrationStatus): StubMapping = {
    val jsonBody = Json.obj("company" ->
      Json.obj(
        "crn" -> crn,
        "ctutr" -> ctutr)
    )
    when(method = POST, uri = "/incorporated-entity-identification/register", jsonBody)
      .thenReturn(
        status = status,
        body = Json.obj("registration" -> body)
      )
  }

  def verifyRegister(crn: String, ctutr: String): Unit = {
    val jsonBody = Json.obj(
      "company" -> Json.obj(
        "crn" -> crn,
        "ctutr" -> ctutr
      )
    )
    WiremockHelper.verifyPost(uri = "/incorporated-entity-identification/register", optBody = Some(jsonBody.toString()))

  }
}
