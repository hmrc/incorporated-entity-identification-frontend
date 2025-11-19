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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.FeatureSwitchRegistry
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.models.FeatureSwitch
import org.scalatest.OptionValues

object A extends FeatureSwitch { val configName = "a"; val displayName = "A" }
object B extends FeatureSwitch { val configName = "b"; val displayName = "B" }

class FeatureSwitchRegistrySpec extends UnitSpec with OptionValues {

  object TestRegistry extends FeatureSwitchRegistry {
    override def switches: Seq[FeatureSwitch] = Seq(A, B)
  }

  "get" should {
    "return Some for known switch" in {
      TestRegistry.get("a").value mustBe A
      TestRegistry.get("b").value mustBe B
    }
    "return None for unknown switch" in {
      TestRegistry.get("c") mustBe None
    }
  }

  "apply" should {
    "return the switch if present" in {
      TestRegistry("a") mustBe A
    }
    "throw for an unknown switch" in {
      val ex = intercept[IllegalArgumentException] { TestRegistry("x") }
      ex.getMessage must include ("Invalid feature switch: x")
    }
  }
}
