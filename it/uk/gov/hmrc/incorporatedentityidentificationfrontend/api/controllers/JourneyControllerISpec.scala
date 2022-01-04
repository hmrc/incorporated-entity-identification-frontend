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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.api.controllers

import play.api.http.Status.CREATED
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.{routes => appRoutes}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, IncorporatedEntityIdentificationStub, JourneyStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper


class JourneyControllerISpec extends ComponentSpecHelper with JourneyStub with IncorporatedEntityIdentificationStub with AuthStub {

  val testJourneyConfigJson: JsObject = Json.obj(
    "continueUrl" -> testLimitedCompanyJourneyConfig.continueUrl,
    "deskProServiceId" -> testLimitedCompanyJourneyConfig.pageConfig.deskProServiceId,
    "signOutUrl" -> testLimitedCompanyJourneyConfig.pageConfig.signOutUrl
  )

  "POST /api/journey" should {
    "return CREATED and supply a journey start url" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/incorporated-entity-identification/api/journey", testJourneyConfigJson)

      result.status mustBe CREATED
      (result.json \ "journeyStartUrl").as[String] must include(appRoutes.CaptureCompanyNumberController.show(testJourneyId).url)

      await(journeyConfigRepository.findJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testLimitedCompanyJourneyConfig)
    }

    "redirect to sign in page" when {
      "the user is not logged in" in {
        stubAuthFailure()
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/incorporated-entity-identification/api/journey", testJourneyConfigJson)

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fincorporated-entity-identification%2Fapi%2Fjourney&origin=incorporated-entity-identification-frontend")
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/incorporated-entity-identification/api/journey", testJourneyConfigJson)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /api/limited-company-journey" should {
    "return CREATED and supply a journey start url" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/incorporated-entity-identification/api/limited-company-journey", testJourneyConfigJson)

      result.status mustBe CREATED
      (result.json \ "journeyStartUrl").as[String] must include(appRoutes.CaptureCompanyNumberController.show(testJourneyId).url)

      await(journeyConfigRepository.findJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testLimitedCompanyJourneyConfig)
    }

    "redirect to sign in page" when {
      "the user is not logged in" in {
        stubAuthFailure()
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/incorporated-entity-identification/api/limited-company-journey", testJourneyConfigJson)

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fincorporated-entity-identification%2Fapi%2Flimited-company-journey&origin=incorporated-entity-identification-frontend")
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/incorporated-entity-identification/api/limited-company-journey", testJourneyConfigJson)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /api/registered-society-journey" should {
    "return CREATED and supply a journey start url" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/incorporated-entity-identification/api/registered-society-journey", testJourneyConfigJson)

      result.status mustBe CREATED
      (result.json \ "journeyStartUrl").as[String] must include(appRoutes.CaptureCompanyNumberController.show(testJourneyId).url)

      await(journeyConfigRepository.findJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testRegisteredSocietyJourneyConfig)
    }

    "redirect to sign in page" when {
      "the user is not logged in" in {
        stubAuthFailure()
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/incorporated-entity-identification/api/registered-society-journey", testJourneyConfigJson)

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fincorporated-entity-identification%2Fapi%2Fregistered-society-journey&origin=incorporated-entity-identification-frontend")
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/incorporated-entity-identification/api/registered-society-journey", testJourneyConfigJson)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /api/charitable-incorporated-organisation-journey" should {
    "return CREATED and supply a journey start url" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

      lazy val result = post("/incorporated-entity-identification/api/charitable-incorporated-organisation-journey", testJourneyConfigJson)

      result.status mustBe CREATED
      (result.json \ "journeyStartUrl").as[String] must include(appRoutes.CaptureCompanyNumberController.show(testJourneyId).url)

      await(journeyConfigRepository.findJourneyConfig(testJourneyId, testInternalId)) mustBe Some(testCharitableIncorporatedOrganisationJourneyConfig)
    }

    "redirect to sign in page" when {
      "the user is not logged in" in {
        stubAuthFailure()
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/incorporated-entity-identification/api/charitable-incorporated-organisation-journey", testJourneyConfigJson)

        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fincorporated-entity-identification%2Fapi%2Fcharitable-incorporated-organisation-journey&origin=incorporated-entity-identification-frontend")
      }
    }

    "throw an Internal Server Exception" when {
      "the user does not have an internal ID" in {
        stubAuth(OK, successfulAuthResponse(None))
        stubCreateJourney(CREATED, Json.obj("journeyId" -> testJourneyId))

        lazy val result = post("/incorporated-entity-identification/api/charitable-incorporated-organisation-journey", testJourneyConfigJson)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "GET /api/journey/:journeyId" should {
    "return captured data" when {
      "the journeyId exists" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = OK,
          body = Json.toJsObject(
            IncorporatedEntityInformation(
              companyProfile = testCompanyProfile,
              optCtutr = Some(testCtutr),
              identifiersMatch = true,
              businessVerification = Some(BusinessVerificationPass),
              registration = testSuccessfulRegistration
            )
          )
        )


        lazy val result = get(s"/incorporated-entity-identification/api/journey/$testJourneyId")

        result.status mustBe OK
        result.json mustBe Json.obj(
          "ctutr" -> testCtutr,
          "companyProfile" -> Json.obj(
            "companyName" -> testCompanyName,
            "companyNumber" -> testCompanyNumber,
            "dateOfIncorporation" -> testDateOfIncorporation,
            "unsanitisedCHROAddress" -> Json.obj(
              "address_line_1" -> "testLine1",
              "address_line_2" -> "test town",
              "care_of" -> "test name",
              "country" -> "United Kingdom",
              "locality" -> "test city",
              "po_box" -> "123",
              "postal_code" -> "AA11AA",
              "premises" -> "1",
              "region" -> "test region"
            )
          ),
          "identifiersMatch" -> true,
          "businessVerification" -> Json.obj(
            "verificationStatus" -> "PASS"
          ),
          "registration" -> Json.obj(
            "registrationStatus" -> "REGISTERED",
            "registeredBusinessPartnerId" -> testSafeId
          )
        )
      }
    }

    "return not found" when {
      "the journey Id does not exist" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubRetrieveIncorporatedEntityInformation(testJourneyId)(
          status = NOT_FOUND
        )

        lazy val result = get(s"/incorporated-entity-identification/api/journey/$testJourneyId")

        result.status mustBe NOT_FOUND
      }
    }

    "return See Other" in {
      stubAuthFailure()
      stubRetrieveIncorporatedEntityInformation(testJourneyId)(
        status = NOT_FOUND
      )

      lazy val result = get(s"/incorporated-entity-identification/api/journey/$testJourneyId")

      result.status mustBe SEE_OTHER
    }
  }
}
