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

import play.api.test.Helpers.OK
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants.{companyNumberKey, testInternalId, testJourneyId}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.AuthStub
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class CaptureBusinessVerificationResultControllerISpec extends ComponentSpecHelper with AuthStub {

  "GET /:journeyId/business-verification-result" should {
    "return Ok" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result = get(s"/$testJourneyId/business-verification-result")

      result.status mustBe 200
    }
  }
}
