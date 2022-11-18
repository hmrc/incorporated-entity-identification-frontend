/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.test.Helpers._
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CompanyProfile, SuccessfullyStored}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.CompanyProfileService
import utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompanyProfileServiceSpec extends UnitSpec
  with MockIncorporatedEntityInformationConnector
  with MockCompanyProfileConnector {

  object TestService extends CompanyProfileService(mockIncorporatedEntityInformationConnector, mockCompanyProfileConnector)

  val dataKey = "companyProfile"
  val testShortCompanyNumber = "1234567"
  val testPaddedCompanyNumber = "01234567"
  val testPrefixedCompanyNumber = "SC12"
  val testPrefixedPaddedCompanyNumber = "SC000012"
  val testSuffixedCompanyNumber = "1234567R"
  val testPrefixSuffixCompanyNumber = "IP1234RS"
  val testInvalidCompanyNumber = "123456 7"


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

      "the company number is 8 characters long" should {
        "return the result of the connector" in {
          mockGetCompanyProfile(testCompanyNumber)(Future.successful(Some(testCompanyProfile)))
          mockStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)(Future.successful(SuccessfullyStored))

          val result = await(TestService.retrieveAndStoreCompanyProfile(testJourneyId, testCompanyNumber))

          result mustBe Some(testCompanyProfile)
          verifyGetCompanyProfile(testCompanyNumber)
          verifyStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)
        }
      }

      "the company number is shorter than 8 characters" when {
        "there CRN has no prefix" should {
          "return the result of the connector" in {
            mockGetCompanyProfile(testPaddedCompanyNumber)(Future.successful(Some(testCompanyProfile)))
            mockStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)(Future.successful(SuccessfullyStored))

            val result = await(TestService.retrieveAndStoreCompanyProfile(testJourneyId, testShortCompanyNumber))

            result mustBe Some(testCompanyProfile)
            verifyGetCompanyProfile(testPaddedCompanyNumber)
            verifyStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)
          }
        }

        "the CRN has a prefix" should {
          "return the result of the connector" in {
            mockGetCompanyProfile(testPrefixedPaddedCompanyNumber)(Future.successful(Some(testCompanyProfile)))
            mockStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)(Future.successful(SuccessfullyStored))

            val result = await(TestService.retrieveAndStoreCompanyProfile(testJourneyId, testPrefixedCompanyNumber))

            result mustBe Some(testCompanyProfile)
            verifyGetCompanyProfile(testPrefixedPaddedCompanyNumber)
            verifyStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)
          }
        }
      }

      "the company number has a suffix" should {
        "return the result of the connector" in {
          mockGetCompanyProfile(testSuffixedCompanyNumber)(Future.successful(Some(testCompanyProfile)))
          mockStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)(Future.successful(SuccessfullyStored))

          val result = await(TestService.retrieveAndStoreCompanyProfile(testJourneyId, testSuffixedCompanyNumber))

          result mustBe Some(testCompanyProfile)
          verifyGetCompanyProfile(testSuffixedCompanyNumber)
          verifyStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)
        }
      }

      "the company number has a prefix and a suffix" should {
        "return the result of the connector" in {
          mockGetCompanyProfile(testPrefixSuffixCompanyNumber)(Future.successful(Some(testCompanyProfile)))
          mockStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)(Future.successful(SuccessfullyStored))

          val result = await(TestService.retrieveAndStoreCompanyProfile(testJourneyId, testPrefixSuffixCompanyNumber))

          result mustBe Some(testCompanyProfile)
          verifyGetCompanyProfile(testPrefixSuffixCompanyNumber)
          verifyStoreData[CompanyProfile](testJourneyId, dataKey, testCompanyProfile)
        }
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

      "the company number is invalid" in {
        intercept[IllegalArgumentException](
          await(TestService.retrieveAndStoreCompanyProfile(testJourneyId, testInvalidCompanyNumber))
        )
      }
    }

  }
}
