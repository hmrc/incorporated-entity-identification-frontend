package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.stubs.controllers

import play.api.libs.json.Json
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.{CompaniesHouseStub, FeatureSwitching}

class CompaniesHouseStubControllerISpec extends ComponentSpecHelper with FeatureSwitching {

  lazy val testCompanyNumber = "12345678"

  s"GET /test-only/companies-house-stub/company/$testCompanyNumber" should {
    lazy val result = get(s"/test-only/companies-house-stub/company/$testCompanyNumber")

    "return OK" in {
      result.status mustBe OK
    }

    "return valid json" in {
      result.json mustBe Json.obj("company_name" -> "Test Company Ltd")
    }
  }

}
