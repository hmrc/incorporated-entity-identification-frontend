/*
 * Copyright 2023 HM Revenue & Customs
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

import com.mongodb.{MongoSocketReadException, ServerAddress}
import connectors.mocks.MockJourneyConnector
import helpers.TestConstants._
import play.api.test.Helpers._
import repositories.mocks.{MockInsertOneResult, MockJourneyConfigRepository}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, NotFoundException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.LimitedCompany
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.JourneyService
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyServiceSpec extends UnitSpec with MockJourneyConnector with MockJourneyConfigRepository with MockInsertOneResult {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestService extends JourneyService(mockJourneyConnector, mockJourneyConfigRepository)

  "createJourney" should {
    "return a journeyID and store the provided journey config" in {
      mockCreateJourney(response = Future.successful(testJourneyId))
      mockWasAcknowledged()(response = true)
      mockInsertJourneyConfig(testJourneyId, testAuthInternalId, testJourneyConfig(LimitedCompany))(response = Future.successful(mockInsertOneResult))

      val result = await(TestService.createJourney(testAuthInternalId, testJourneyConfig(LimitedCompany)))

      result mustBe testJourneyId
      verifyCreateJourney()
      verifyInsertJourneyConfig(testJourneyId, testAuthInternalId, testJourneyConfig(LimitedCompany))
      verifyWasAcknowledged()
    }

    "throw an exception" when {
      "create journey API returns an invalid response" in {
        mockCreateJourney(response = Future.failed(new InternalServerException("Invalid response returned from create journey API")))

        intercept[InternalServerException](
          await(TestService.createJourney(testAuthInternalId, testJourneyConfig(LimitedCompany)))
        )
        verifyCreateJourney()
      }

      "the storing of the journey configuration is not acknowledged" in {
        mockCreateJourney(response = Future.successful(testJourneyId))
        mockWasAcknowledged()(response = false)
        mockInsertJourneyConfig(testJourneyId, testAuthInternalId, testJourneyConfig(LimitedCompany)
        )(response = Future.successful(mockInsertOneResult))

        intercept[InternalServerException](
          await(TestService.createJourney(testAuthInternalId, testJourneyConfig(LimitedCompany)))
        )

        verifyCreateJourney()
        verifyInsertJourneyConfig(testJourneyId, testAuthInternalId, testJourneyConfig(LimitedCompany))
        verifyWasAcknowledged()
      }

      "an exception is raised on storing the data" in {
        mockCreateJourney(response = Future.successful(testJourneyId))
        mockInsertJourneyConfig(testJourneyId, testAuthInternalId, testJourneyConfig(LimitedCompany))(response =
          Future.failed(new MongoSocketReadException("Exception receiving message", new ServerAddress())))

        intercept[MongoSocketReadException](
          await(TestService.createJourney(testAuthInternalId, testJourneyConfig(LimitedCompany)))
        )
        verifyCreateJourney()
        verifyInsertJourneyConfig(testJourneyId, testAuthInternalId, testJourneyConfig(LimitedCompany))
      }

    }
  }

  "getJourneyConfig" should {
    "return the journey config for a specific journey id and auth internal ID" when {
      "the journey id and auth internal id exists in the database" in {
        mockFindJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(Some(testJourneyConfig(LimitedCompany))))

        val result = await(TestService.getJourneyConfig(testJourneyId, testAuthInternalId))

        result mustBe testJourneyConfig(LimitedCompany)
        verifyFindJourneyConfig(testJourneyId, testAuthInternalId)
      }
    }

    "throw a Not Found Exception" when {
      "the journey config does not exist in the database" in {
        mockFindJourneyConfig(testJourneyId, testAuthInternalId)(Future.successful(None))

        intercept[NotFoundException](
          await(TestService.getJourneyConfig(testJourneyId, testAuthInternalId))
        )

        verifyFindJourneyConfig(testJourneyId, testAuthInternalId)
      }
    }
  }

}
