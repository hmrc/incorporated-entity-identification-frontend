/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import connectors.mocks.MockIncorporatedEntityInformationConnector
import helpers.TestConstants._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.StorageService
import utils.UnitSpec

import scala.concurrent.Future

class StorageServiceSpec extends UnitSpec with MockIncorporatedEntityInformationConnector {

  object TestService extends StorageService(mockIncorporatedEntityInformationConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val dataKey = "ctutr"

  "retrieveCtutr" should {
    "return Some(ctutr)" when {
      "the ctutr exists in the database for a given journey id" in {
        mockRetrieveIncorporatedEntityInformation[String](
          testJourneyId,
          dataKey
        )(Future.successful(Some(testCtutr)))

        val result = await(TestService.retrieveCtutr(testJourneyId))

        result mustBe Some(testCtutr)
        verifyRetrieveIncorporatedEntityInformation[String](testJourneyId, dataKey)
      }
    }

    "return None" when {
      "the ctutr does not exist in the database for a given journey id" in {
        mockRetrieveIncorporatedEntityInformation[String](
          testJourneyId,
          dataKey
        )(Future.successful(None))

        val result = await(TestService.retrieveCtutr(testJourneyId))

        result mustBe None
        verifyRetrieveIncorporatedEntityInformation[String](testJourneyId, dataKey)
      }
    }

    "surface an exception" when {
      "the call to the database times out" in {
        mockRetrieveIncorporatedEntityInformation[String](
          journeyId = testJourneyId,
          dataKey = "ctutr"
        )(Future.failed(new GatewayTimeoutException("GET of '/testUrl' timed out with message 'testError'")))

        intercept[GatewayTimeoutException](
          await(TestService.retrieveCtutr(testJourneyId))
        )
        verifyRetrieveIncorporatedEntityInformation[String](testJourneyId, dataKey)
      }
    }
  }

}
