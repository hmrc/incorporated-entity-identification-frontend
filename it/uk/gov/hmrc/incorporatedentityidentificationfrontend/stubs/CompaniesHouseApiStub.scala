package uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.WireMockMethods

trait CompaniesHouseApiStub extends WireMockMethods {

  def stubRetrieveCompanyInformation(companyNumber: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/test-only/incorporation-information/$companyNumber/incorporated-company-profile")
      .thenReturn(
        status = status,
        body = body
      )
}
