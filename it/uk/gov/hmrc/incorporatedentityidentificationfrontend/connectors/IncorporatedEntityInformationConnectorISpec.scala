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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.connectors

import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.httpparsers.RemoveIncorporatedEntityDetailsHttpParser.SuccessfullyRemoved
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.IncorporatedEntityIdentificationStub
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

class IncorporatedEntityInformationConnectorISpec extends ComponentSpecHelper with IncorporatedEntityIdentificationStub {

  private val incorporatedEntityInformationConnector = app.injector.instanceOf[IncorporatedEntityInformationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val ctutrKey: String = "ctutr"
  val companyProfileKey: String = "companyProfile"
  val verificationStatusKey: String = "businessVerification"
  val identifiersMatchKey: String = "identifiersMatch"

  s"retrieveIncorporatedEntityInformation($testJourneyId)" should {
    "return Incorporated Entity Information" when {
      "there is Incorporated Entity Information stored against the journeyId" in {
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = OK,
          body = Json.toJsObject(
            IncorporatedEntityInformation(
              companyProfile = testCompanyProfile,
              optCtutr = Some(testCtutr),
              identifiersMatch = true,
              businessVerification = BusinessVerificationPass,
              registration = testSuccessfulRegistration
            )
          )
        )

        val result = await(incorporatedEntityInformationConnector.retrieveIncorporatedEntityInformation(testJourneyId))

        result mustBe Some(
          IncorporatedEntityInformation(
            testCompanyProfile,
            Some(testCtutr),
            true,
            BusinessVerificationPass,
            testSuccessfulRegistration))
      }
    }
    "return None" when {
      "there is no Incorporated Entity Information stored against the journeyId" in {
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = NOT_FOUND
        )
        val result = await(incorporatedEntityInformationConnector.retrieveIncorporatedEntityInformation(testJourneyId))

        result mustBe None
      }
    }
  }
  s"retrieveIncorporatedEntityInformation($testJourneyId, $companyProfileKey)" should {
    "return CompanyProfile" when {
      "the companyProfile key is given there is a Company Profile stored against the journeyId" in {
        stubRetrieveCompanyProfileFromBE(testJourneyId)(OK, Json.toJsObject(testCompanyProfile))

        val result = await(incorporatedEntityInformationConnector.retrieveIncorporatedEntityInformation[CompanyProfile](testJourneyId, companyProfileKey))

        result mustBe Some(testCompanyProfile)
      }
    }
    "return None" when {
      "the companyProfile key is given but there is no company profile stored against the journeyId" in {
        stubRetrieveCompanyProfileFromBE(testJourneyId)(NOT_FOUND)

        val result = await(incorporatedEntityInformationConnector.retrieveIncorporatedEntityInformation[CompanyProfile](testJourneyId, companyProfileKey))

        result mustBe None
      }
    }
  }
  s"retrieveIncorporatedEntityInformation($testJourneyId, $ctutrKey)" should {
    "return Ctutr" when {
      "the ctutr key is given and a ctutr is stored against the journeyId" in {
        stubRetrieveCtutr(testJourneyId)(OK, testCtutr)

        val result = await(incorporatedEntityInformationConnector.retrieveIncorporatedEntityInformation[JsString](testJourneyId, ctutrKey))

        result mustBe Some(JsString(testCtutr))
      }
    }
    "return None" when {
      "no ctutr is stored against the journeyId" in {
        stubRetrieveCtutr(testJourneyId)(NOT_FOUND)

        val result = await(incorporatedEntityInformationConnector.retrieveIncorporatedEntityInformation[JsString](testJourneyId, ctutrKey))

        result mustBe None
      }
    }
  }
  s"storeData($testJourneyId, $companyProfileKey, CompanyProfile)" should {
    "return SuccessfullyStored" in {
      stubStoreCompanyProfile(testJourneyId, testCompanyProfile)(status = OK)

      val result = await(incorporatedEntityInformationConnector.storeData[CompanyProfile](
        testJourneyId, companyProfileKey, testCompanyProfile))

      result mustBe SuccessfullyStored
    }
  }
  s"storeData($testJourneyId, $verificationStatusKey, BusinessVerificationStatus)" should {
    "return SuccessfullyStored" in {
      stubStoreBusinessVerificationStatus(testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)

      val result = await(incorporatedEntityInformationConnector.storeData[BusinessVerificationStatus](testJourneyId, verificationStatusKey, BusinessVerificationPass))

      result mustBe SuccessfullyStored
    }
  }
  s"storeData($testJourneyId, $ctutrKey)" should {
    "return SuccessfullyStored" in {
      stubStoreCtutr(testJourneyId, testCtutr)(status = OK)

      val result = await(incorporatedEntityInformationConnector.storeData[String](testJourneyId, ctutrKey, testCtutr))

      result mustBe SuccessfullyStored
    }
  }
  s"storeData($testJourneyId, $identifiersMatchKey)" should {
    "return SuccessfullyStored" in {
      stubStoreIdentifiersMatch(testJourneyId,identifiersMatch = true)(status = OK)

      val result = await(incorporatedEntityInformationConnector.storeData[Boolean](testJourneyId, identifiersMatchKey, true))

      result mustBe SuccessfullyStored
    }
  }
  s"removeIncorporatedEntityDetailsField($testJourneyId, $ctutrKey)" should {
    "return SuccessfullyRemoved" when {
      "the ctutr was successfully removed from the database" in {
        stubRemoveCtutr(testJourneyId)(NO_CONTENT)
        val result = await(incorporatedEntityInformationConnector.removeIncorporatedEntityDetailsField(testJourneyId, ctutrKey))

        result mustBe SuccessfullyRemoved
      }
    }

    "throw an exception" when {
      "the sautr could not be deleted" in {
        stubRemoveCtutr(testJourneyId)(INTERNAL_SERVER_ERROR, "Failed to remove field")

        intercept[InternalServerException] {
          await(incorporatedEntityInformationConnector.removeIncorporatedEntityDetailsField(testJourneyId, ctutrKey))
        }
      }
    }
  }
}
