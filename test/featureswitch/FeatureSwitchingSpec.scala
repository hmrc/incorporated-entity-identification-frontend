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

import org.scalatest.BeforeAndAfterEach
import utils.UnitSpec
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.models.FeatureSwitch

object TestSwitch extends FeatureSwitch {
  override val configName: String = "feature-switch.test"
  override val displayName: String = "Test switch"
}

class FeatureSwitchingSpec extends UnitSpec with BeforeAndAfterEach with FeatureSwitching {

  override def afterEach(): Unit = {
    sys.props -= TestSwitch.configName
    super.afterEach()
  }

  "enable" should {
    "set the system property to true" in {
      enable(TestSwitch)
      isEnabled(TestSwitch) mustBe true
      sys.props(TestSwitch.configName) mustBe FEATURE_SWITCH_ON
    }
  }

  "disable" should {
    "set the system property to false" in {
      disable(TestSwitch)
      isEnabled(TestSwitch) mustBe false
      sys.props(TestSwitch.configName) mustBe FEATURE_SWITCH_OFF
    }
  }

  "isEnabled" should {
    "be true only when the prop is exactly 'true'" in {
      sys.props += TestSwitch.configName -> FEATURE_SWITCH_ON
      isEnabled(TestSwitch) mustBe true

      sys.props += TestSwitch.configName -> "TRUE"
      isEnabled(TestSwitch) mustBe false

      sys.props += TestSwitch.configName -> FEATURE_SWITCH_OFF
      isEnabled(TestSwitch) mustBe false

      sys.props -= TestSwitch.configName
      isEnabled(TestSwitch) mustBe false
    }
  }
}
