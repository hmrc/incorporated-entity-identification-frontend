
package uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.RegistrationStatus
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.{WireMockMethods, WiremockHelper}

trait RegisterStub extends WireMockMethods {

  private def jsonBody(crn: String, ctutr: String): JsObject = {
    Json.obj(
      "crn" -> crn,
      "ctutr" -> ctutr)
  }

  def stubLimitedCompanyRegister(crn: String, ctutr: String)(status: Int, body: RegistrationStatus): StubMapping = {
    when(method = POST, uri = "/incorporated-entity-identification/register-limited-company", jsonBody(crn, ctutr))
      .thenReturn(
        status = status,
        body = Json.obj("registration" -> body)
      )
  }

  def verifyLimitedCompanyRegister(crn: String, ctutr: String): Unit = {
    WiremockHelper.verifyPost(uri = "/incorporated-entity-identification/register-limited-company", optBody = Some(jsonBody(crn, ctutr).toString()))
  }

  def stubRegisteredSocietyRegister(crn: String, ctutr: String)(status: Int, body: RegistrationStatus): StubMapping = {
    when(method = POST, uri = "/incorporated-entity-identification/register-registered-society", jsonBody(crn, ctutr))
      .thenReturn(
        status = status,
        body = Json.obj("registration" -> body)
      )
  }

  def verifyRegisteredSocietyRegister(crn: String, ctutr: String): Unit = {
    WiremockHelper.verifyPost(uri = "/incorporated-entity-identification/register-registered-society", optBody = Some(jsonBody(crn, ctutr).toString()))
  }

  def verifyRegisterAudit(): Unit = {
    WiremockHelper.verifyPost(uri = "/write/audit", optBody = Some("Audit"))
//    WiremockHelper.verifyPost(uri = "/write/audit/merged")
//    WiremockHelper.verifyPost(uri = "/write/audit")
  }
}
