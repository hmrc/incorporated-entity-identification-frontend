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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import org.jsoup.nodes.Document
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.{TableFor1, TableFor2, Tables}
import play.api.libs.json.Json
import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

class SupportWelshTranslationControllerISpec extends ComponentSpecHelper
  with IncorporatedEntityIdentificationStub
  with AuthStub {

  "all the views requested via GET" should {

    object TestPrecondition {
      type TestPrecondition = () => Any
      val noNeeded: TestPrecondition = () => ()
      val checkYourAnswers: TestPrecondition = () => stubRetrieveIncorporatedEntityInformation(testJourneyId)(OK, testIncorporatedEntityFullJourneyDataJson)
      val confirmCompanyName: TestPrecondition = () => stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
      val confirmCtutr: TestPrecondition = () => stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)
      val confirmChrn: TestPrecondition = () => stubRetrieveChrn(testJourneyId)(status = OK, body = testCHRN)
      val confirmBusinessName: TestPrecondition = () => noNeeded

    }

    val allGETUrlsToBeTested: TableFor2[String, TestPrecondition.TestPrecondition] =
      Tables.Table(
        ("urlToBeTested", "doThisToCreateTestPrecondition"),
        (s"$baseUrl/$testJourneyId/company-number", TestPrecondition.confirmCompanyName),
        (s"$baseUrl/$testJourneyId/check-your-answers-business", TestPrecondition.checkYourAnswers),
        (s"$baseUrl/$testJourneyId/ct-utr", TestPrecondition.confirmCtutr),
        (s"$baseUrl/$testJourneyId/confirm-business-name", TestPrecondition.confirmBusinessName),
        (s"$baseUrl/$testJourneyId/chrn", TestPrecondition.confirmChrn)
      )

    "display welsh translation when cy cookie is specified" in {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testRegisteredSocietyJourneyConfig
      ))

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      forAll(allGETUrlsToBeTested) { (getUrlToBeTested, doThisToCreateTestPrecondition) =>

        lazy val actualDocFromResponse: Document = {
          doThisToCreateTestPrecondition()
          extractDocumentFrom(aWSResponse = get(getUrlToBeTested, cookie = cyLangCookie))
        }

        actualDocFromResponse.getServiceName.text mustBe testDefaultWelshServiceName

      }
    }

    "display the custom welsh translation in the pageConfig when cy cookie is specified" in {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testDefaultWelshJourneyConfig
      ))

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      forAll(allGETUrlsToBeTested) { (getUrlToBeTested, doThisToCreateTestPrecondition) =>

        lazy val actualDocFromResponse: Document = {
          doThisToCreateTestPrecondition()
          extractDocumentFrom(aWSResponse = get(getUrlToBeTested, cookie = cyLangCookie))
        }

        actualDocFromResponse.getServiceName.text mustBe testWelshServiceName

      }
    }

  }

  "all the views in case of error (after a POST)" should {

    val allPOSTUrlsToBeTested: TableFor1[String] =
      Tables.Table(
        "urlToBeTested",
        s"$baseUrl/$testJourneyId/company-number",
        s"$baseUrl/$testJourneyId/ct-utr",
        s"$baseUrl/$testJourneyId/chrn"
      )

    "display welsh translation when cy cookie is specified" in {

      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testRegisteredSocietyJourneyConfig
      )
      )

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      forAll(allPOSTUrlsToBeTested) { urlToBeTested =>

        lazy val actualDocFromResponse: Document = {
          val anInvalidFormToCauseAnError = "somethingWrong" -> ""
          extractDocumentFrom(aWSResponse = post(urlToBeTested, cookie = cyLangCookie)(form = anInvalidFormToCauseAnError))
        }

        actualDocFromResponse.getServiceName.text mustBe testDefaultWelshServiceName

      }
    }

    "display the custom welsh translation in the pageConfig when cy cookie is specified" in {
      await(journeyConfigRepository.insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        journeyConfig = testDefaultWelshJourneyConfig
      ))

      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      forAll(allPOSTUrlsToBeTested) { urlToBeTested =>

        lazy val actualDocFromResponse: Document = {
          val anInvalidFormToCauseAnError = "somethingWrong" -> ""
          extractDocumentFrom(aWSResponse = post(urlToBeTested, cookie = cyLangCookie)(anInvalidFormToCauseAnError))
        }

        actualDocFromResponse.getServiceName.text mustBe testWelshServiceName

      }
    }

  }

}
