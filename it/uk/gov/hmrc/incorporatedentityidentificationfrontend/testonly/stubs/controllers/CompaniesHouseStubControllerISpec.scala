/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.stubs.controllers

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class CompaniesHouseStubControllerISpec extends ComponentSpecHelper with FeatureSwitching {

  s"GET /test-only/incorporation-information/$testCompanyNumber/incorporated-company-profile" should {
    lazy val result = get(s"/test-only/incorporation-information/$testCompanyNumber/incorporated-company-profile")

    "return OK" in {
      result.status mustBe OK
    }

    "return valid json" in {
      result.json mustBe Json.obj(coHoCompanyNameKey -> testCompanyName)
    }
  }

}
