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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.{CompaniesHouseStub, FeatureSwitching}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{CompaniesHouseApiStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class CompanyProfileConnectorISpec extends ComponentSpecHelper with CompaniesHouseApiStub with IncorporatedEntityIdentificationStub with FeatureSwitching {

  private lazy val companyProfileConnector: CompanyProfileConnector = app.injector.instanceOf[CompanyProfileConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "getCompanyProfile" should {
    "return Company profile" when {
      "the companyNumber exists and the feature switch is enabled" in {
        enable(CompaniesHouseStub)
        stubRetrieveCompanyProfileFromStub(testCompanyNumber)(
          status = OK,
          body = companyProfileJson(testCompanyNumber, testCompanyName, testDateOfIncorporation, testAddress)
        )
        stubStoreCompanyProfile(testJourneyId, testCompanyProfile)(status = OK)

        val result = await(companyProfileConnector.getCompanyProfile(testCompanyNumber))

        result mustBe Some(testCompanyProfile)
      }

      "the companyNumber exists and the feature switch is disabled" in {
        disable(CompaniesHouseStub)
        stubRetrieveCompanyProfileFromCoHo(testCompanyNumber)(
          status = OK,
          body = companyProfileJson(testCompanyNumber, testCompanyName, testDateOfIncorporation, testAddress)
        )
        stubStoreCompanyProfile(testJourneyId, testCompanyProfile)(status = OK)

        val result = await(companyProfileConnector.getCompanyProfile(testCompanyNumber))

        result mustBe Some(testCompanyProfile)
      }

      "the company Number in lower case is provided and the feature switch is enabled" in {
        enable(CompaniesHouseStub)
        stubRetrieveCompanyProfileFromStub(testCompanyNumberInUppercase)(
          status = OK,
          body = companyProfileJson(testCompanyNumberInUppercase, testCompanyName, testDateOfIncorporation, testAddress)
        )
        stubStoreCompanyProfile(testJourneyId, testCompanyProfile.copy(companyNumber = testCompanyNumberInUppercase))(status = OK)

        val result = await(companyProfileConnector.getCompanyProfile(testCompanyNumberInUppercase.toLowerCase))

        result mustBe Some(testCompanyProfile.copy(companyNumber = testCompanyNumberInUppercase))
      }

      "the company Number in lower case is provided and the feature switch is disabled" in {
        disable(CompaniesHouseStub)
        stubRetrieveCompanyProfileFromCoHo(testCompanyNumberInUppercase)(
          status = OK,
          body = companyProfileJson(testCompanyNumberInUppercase, testCompanyName, testDateOfIncorporation, testAddress)
        )
        stubStoreCompanyProfile(testJourneyId, testCompanyProfile.copy(companyNumber = testCompanyNumberInUppercase))(status = OK)

        val result = await(companyProfileConnector.getCompanyProfile(testCompanyNumberInUppercase.toLowerCase))

        result mustBe Some(testCompanyProfile.copy(companyNumber = testCompanyNumberInUppercase))
      }

      "the companyNumber exists but the creation of date is missing" in {
        enable(CompaniesHouseStub)
        stubRetrieveCompanyProfileFromStub(testCompanyNumber)(
          status = OK,
          body = companyProfileJson(testCompanyNumber, testCompanyName, None, testAddress)
        )
        stubStoreCompanyProfile(testJourneyId, testCompanyProfile)(status = OK)

        val result = await(companyProfileConnector.getCompanyProfile(testCompanyNumber))

        result mustBe Some(testCompanyProfile.copy(dateOfIncorporation = None))
      }
    }

    "return None" when {
      "the companyNumber cannot be found and the feature switch is enabled" in {
        enable(CompaniesHouseStub)
        stubRetrieveCompanyProfileFromStub(testCompanyNumber)(status = NOT_FOUND)

        val result = await(companyProfileConnector.getCompanyProfile(testCompanyNumber))

        result mustBe None
      }

      "the companyNumber cannot be found and the feature switch is disabled" in {
        disable(CompaniesHouseStub)
        stubRetrieveCompanyProfileFromCoHo(testCompanyNumber)(status = NOT_FOUND)

        val result = await(companyProfileConnector.getCompanyProfile(testCompanyNumber))

        result mustBe None
      }
    }

    "return Internal Server Exception" when {
      "the Company House return other than 200, OK" in {
        enable(CompaniesHouseStub)
        stubRetrieveCompanyProfileFromStub(testCompanyNumber)(status = INTERNAL_SERVER_ERROR)

        val thrown = the [InternalServerException] thrownBy await(companyProfileConnector.getCompanyProfile(testCompanyNumber))

        thrown.getMessage mustBe "Companies House API failed with status: 500"
      }
    }
  }
}
