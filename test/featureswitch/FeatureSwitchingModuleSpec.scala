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

package featureswitch

import utils.UnitSpec
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config._

class FeatureSwitchingModuleSpec extends UnitSpec {

  "FeatureSwitchingModule" should {
    "bind FeatureSwitchRegistry to FeatureSwitchRegistryImpl and expose default switches" in {
      val app = new GuiceApplicationBuilder()
        .bindings(new FeatureSwitchingModule)
        .build()

      val reg = app.injector.instanceOf[FeatureSwitchRegistry]
      reg mustBe a[FeatureSwitchRegistryImpl]

      val switches = reg.switches
      switches must contain (CompaniesHouseStub)
      switches must contain (BusinessVerificationStub)
    }

    "have expected names for built-in switches" in {
      CompaniesHouseStub.configName mustBe "feature-switch.companies-house-stub"
      CompaniesHouseStub.displayName must include ("Companies House")

      BusinessVerificationStub.configName mustBe "feature-switch.business-verification-stub"
      BusinessVerificationStub.displayName must include ("Business Verification")
    }
  }
}
