/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import org.scalatest.BeforeAndAfterEach
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.{BusinessVerificationStub, CompaniesHouseStub}
import utils.UnitSpec

class AppConfigSpec extends UnitSpec with BeforeAndAfterEach {

  private def app(extra: Map[String, Any] = Map.empty) =
    new GuiceApplicationBuilder()
      .configure(
        Map(
          "microservice.services.self.protocol" -> "http",
          "microservice.services.self.host" -> "localhost",
          "microservice.services.self.port" -> 9000,
          "microservice.services.self.url" -> "http://localhost:9000",
          "microservice.services.incorporation-information.protocol" -> "http",
          "microservice.services.incorporation-information.host" -> "inc-info",
          "microservice.services.incorporation-information.port" -> 9800,
          "microservice.services.business-verification.url" -> "http://bv:9800"
        ) ++ extra
      ).build()

  override def afterEach(): Unit = {
    sys.props -= CompaniesHouseStub.configName
    sys.props -= BusinessVerificationStub.configName
    super.afterEach()
  }

  "getCompanyProfileUrl" should {
    "use incorporation-information when CompaniesHouseStub disabled" in {
      val application = app()
      val config = application.injector.instanceOf[AppConfig]
      config.isEnabled(CompaniesHouseStub) mustBe false
      config.getCompanyProfileUrl("12345678") mustBe "http://inc-info:9800/incorporation-information/12345678/incorporated-company-profile"
      application.stop()
    }
    "use selfBaseUrl test-only path when CompaniesHouseStub enabled" in {
      sys.props += CompaniesHouseStub.configName -> "true"
      val application = app()
      val config = application.injector.instanceOf[AppConfig]
      config.isEnabled(CompaniesHouseStub) mustBe true
      config.getCompanyProfileUrl("12345678") mustBe "http://localhost:9000/identify-your-incorporated-business/test-only/12345678/incorporated-company-profile"
      application.stop()
    }
  }

  "createBusinessVerificationJourneyUrl and getBusinessVerificationResultUrl" should {
    "use BV service URLs by default and self when stub enabled" in {
      val application1 = app()
      val config1 = application1.injector.instanceOf[AppConfig]
      config1.createBusinessVerificationJourneyUrl mustBe "http://bv:9800/journey"
      config1.getBusinessVerificationResultUrl("jid") mustBe "http://bv:9800/journey/jid/status"
      application1.stop()

      sys.props += BusinessVerificationStub.configName -> "true"
      val application2 = app()
      val config2 = application2.injector.instanceOf[AppConfig]
      config2.createBusinessVerificationJourneyUrl mustBe "http://localhost:9000/identify-your-incorporated-business/test-only/business-verification/journey"
      config2.getBusinessVerificationResultUrl("jid") mustBe "http://localhost:9000/identify-your-incorporated-business/test-only/business-verification/journey/jid/status"
      application2.stop()
    }
  }
}
