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

package services

import connectors.mocks.MockJourneyConnector
import helpers.TestConstants._
import reactivemongo.api.commands.WriteResult
import repositories.mocks.MockJourneyConfigRepository
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.JourneyService
import utils.UnitSpec
import play.api.test.Helpers._
import reactivemongo.core.errors.GenericDriverException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class JourneyServiceSpec extends UnitSpec with MockJourneyConnector with MockJourneyConfigRepository {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestService extends JourneyService(mockJourneyConnector, mockJourneyConfigRepository)

  val testJourneyConfig: JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl
    )
  )

  "createJourney" should {
    "return a journeyID and store the provided journey config" in {
      mockCreateJourney(response = Future.successful(testJourneyId))
      mockInsertJourneyConfig(testJourneyId, testJourneyConfig)(response = Future.successful(mock[WriteResult]))

      val result = await(TestService.createJourney(testJourneyConfig))

      result mustBe testJourneyId
      verifyCreateJourney()
      verifyInsertJourneyConfig(testJourneyId, testJourneyConfig)
    }

    "throw an exception" when {
      "create journey API returns an invalid response" in {
        mockCreateJourney(response = Future.failed(new InternalServerException("Invalid response returned from create journey API")))
        mockInsertJourneyConfig(testJourneyId, testJourneyConfig)(response = Future.successful(mock[WriteResult]))

        intercept[InternalServerException](
          await(TestService.createJourney(testJourneyConfig))
        )
        verifyCreateJourney()
      }

      "the journey config is not stored" in {
        mockCreateJourney(response = Future.successful(testJourneyId))
        mockInsertJourneyConfig(testJourneyId, testJourneyConfig)(response = Future.failed(GenericDriverException("failed to insert")))

        intercept[GenericDriverException](
          await(TestService.createJourney(testJourneyConfig))
        )
        verifyCreateJourney()
        verifyInsertJourneyConfig(testJourneyId, testJourneyConfig)
      }
    }
  }

  "getJourneyConfig" should {
    "return the journey config for a specific journey id" when {
      "the journey id exists in the database" in {
        mockFindById(testJourneyId)(Future.successful(Some(testJourneyConfig)))

        val result = await(TestService.getJourneyConfig(testJourneyId))

        result mustBe testJourneyConfig
        verifyFindById(testJourneyId)
      }
    }

    "throw an Internal Server Exception" when {
      "the journey config does not exist in the database" in {
        mockFindById(testJourneyId)(Future.successful(None))

        intercept[InternalServerException](
          await(TestService.getJourneyConfig(testJourneyId))
        )
        verifyFindById(testJourneyId)
      }
    }
  }

}
