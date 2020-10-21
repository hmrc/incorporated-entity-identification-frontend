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

import connectors.mocks.{MockCompanyProfileConnector, MockIncorporatedEntityInformationConnector}
import helpers.TestConstants._
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CompanyProfile, SuccessfullyStored}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.CompanyProfileService
import play.api.test.Helpers._
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompanyProfileServiceSpec extends UnitSpec
  with MockIncorporatedEntityInformationConnector
  with MockCompanyProfileConnector {

  object TestService extends CompanyProfileService(mockIncorporatedEntityInformationConnector, mockCompanyProfileConnector)

  val dataKey = "companyProfile"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "retrieveAndStoreCompanyProfile" should {
    "return Some(CompanyProfile)" when {
      "the company profile model has been stored in the database" in {
        mockGetCompanyProfile(testCompanyNumber)(Future.successful(Some(testCompanyProfile)))
        mockStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)(Future.successful(SuccessfullyStored))

        val result = await(TestService.retrieveAndStoreCompanyProfile(testJourneyId, testCompanyNumber))

        result mustBe Some(testCompanyProfile)
        verifyGetCompanyProfile(testCompanyNumber)
        verifyStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)
      }
    }

    "throw an exception" when {
      "the call to the database times out" in {
        mockGetCompanyProfile(testCompanyNumber)(Future.successful(Some(testCompanyProfile)))
        mockStoreData(
          testJourneyId,
          dataKey,
          testCompanyProfile
        )(Future.failed(new GatewayTimeoutException("GET of '/testUrl' timed out with message 'testError'")))

        intercept[GatewayTimeoutException](
          await(TestService.retrieveAndStoreCompanyProfile(testJourneyId, testCompanyNumber))
        )
        verifyGetCompanyProfile(testCompanyNumber)
        verifyStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)
      }
    }

    "return None" when {
      "there is no company profile for the given company number" in {
        mockGetCompanyProfile(testCompanyNumber)(Future.successful(None))

        val result = await(TestService.retrieveAndStoreCompanyProfile(testJourneyId, testCompanyNumber))

        result mustBe None
        verifyGetCompanyProfile(testCompanyNumber)
      }
    }
  }

}
