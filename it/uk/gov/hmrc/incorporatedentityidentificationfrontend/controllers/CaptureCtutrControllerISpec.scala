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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CaptureCtutrViewTests

class CaptureCtutrControllerISpec extends ComponentSpecHelper with CaptureCtutrViewTests {
  val testCtutr = "1234567890"

  "GET /ct-utr" should {
    lazy val result = get("/ct-utr")

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureCtutrView(result)
    }
  }

  "POST /ct-utr" should {
    "redirect to Check Your Answers page" in {
      val result = post("/ct-utr")("ctutr" -> testCtutr)
      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CheckYourAnswersController.show().url)
      )
    }

    "no ctutr is submitted" should {
      lazy val result = post("/ct-utr")("ctutr" -> "")

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureCtutrErrorMessagesNoCtutr(result)
    }

    "an invalid ctutr is submitted" should {
      lazy val result = post("/ct-utr")("ctutr" -> "123456789")

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureCtutrErrorMessagesInvalidCtutr(result)
    }
  }

}
