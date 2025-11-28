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

package forms.utils

import utils.UnitSpec
import play.api.data._
import play.api.data.Forms._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.MappingUtil
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.MappingUtil.OTextUtil

class MappingUtilSpec extends UnitSpec {

  private def bind[A](m: Mapping[A], data: Map[String,String], key: String = "k"): A = {
    val form = Form(single(key -> m))
    form.bind(data).get
  }

  private def unbind[A](m: Mapping[A], value: A, key: String = "k"): Map[String,String] = {
    val form = Form(single(key -> m))
    form.fill(value).data
  }

  "optText" should {
    "bind Some and None" in {
      bind(MappingUtil.optText, Map("k" -> "abc")) mustBe Some("abc")
      bind(MappingUtil.optText, Map.empty) mustBe None
    }
  }

  "toText" should {
    "map Some(x) to x and None to empty string" in {
      val m = MappingUtil.optText.toText
      bind(m, Map("k" -> "hello")) mustBe "hello"
      // when no key present, MappingUtil.optText binds None which then transforms to ""
      unbind(m, "world")("k") mustBe "world"
    }
  }

  "toBoolean" should {
    "map Some(true) to true and others to false; reverse maps to strings" in {
      val m = MappingUtil.optText.toBoolean
      bind(m, Map("k" -> "true")) mustBe true
      bind(m, Map("k" -> "anything")) mustBe false
      bind(m, Map.empty) mustBe false

      unbind(m, true)("k") mustBe "true"
      unbind(m, false)("k") mustBe "false"
    }
  }
}
