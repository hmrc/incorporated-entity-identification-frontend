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

package utils

import org.scalatest.BeforeAndAfterAll
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.UrlHelper

class UrlHelperSpec extends UnitSpec with BeforeAndAfterAll {

  private val app = new GuiceApplicationBuilder()
    .configure(
      // allow only this host for absolute URLs
      "microservice.hosts.allowList" -> Seq("allowed.com")
    )
    .build()

  private val injector: play.api.inject.Injector = app.injector
  private val urlHelper: UrlHelper = injector.instanceOf[UrlHelper]

  override def afterAll(): Unit = {
    app.stop()
    super.afterAll()
  }

  "areRelativeOrAcceptedUrls" should {
    "return true for relative paths" in {
      urlHelper.areRelativeOrAcceptedUrls(List("/foo", "/bar/baz")) mustBe true
    }

    "return true for absolute URL with allowed host" in {
      urlHelper.areRelativeOrAcceptedUrls(List("https://allowed.com/path")) mustBe true
    }

    "return false for absolute URL with disallowed host" in {
      urlHelper.areRelativeOrAcceptedUrls(List("https://evil.com/path")) mustBe false
    }

    "return false if any URL is invalid or disallowed" in {
      urlHelper.areRelativeOrAcceptedUrls(List("/ok", "ht!tp://bad")) mustBe false
    }
  }
}
