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

import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages.{routes => errorRoutes}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.featureswitch.core.config.{EnableUnmatchedCtutrJourney, FeatureSwitching}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationUnchallenged, RegistrationNotCalled}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs.{AuthStub, BusinessVerificationStub, IncorporatedEntityIdentificationStub}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.CheckYourAnswersViewTests


class CheckYourAnswersControllerISpec extends ComponentSpecHelper
  with CheckYourAnswersViewTests
  with IncorporatedEntityIdentificationStub
  with BusinessVerificationStub
  with AuthStub
  with FeatureSwitching {

  "GET /check-your-answers-business" should {
    "return OK" in {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        continueUrl = testContinueUrl,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        businessEntity = LimitedCompany
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
      stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)

      lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

      result.status mustBe OK
    }

    "return a view" when {
      "there is no serviceName passed in the journeyConfig" should {
        lazy val insertConfig = insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          businessEntity = LimitedCompany
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val companyNumberStub = stubRetrieveCompanyProfileFromBE(testJourneyId)(
          status = OK,
          body = Json.toJsObject(testCompanyProfile)
        )
        lazy val ctutrStub = stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)

        lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        testCheckYourAnswersView(testJourneyId)(result, companyNumberStub, ctutrStub, authStub, insertConfig)
        testServiceName(testDefaultServiceName, result, authStub, insertConfig)
      }

      "there is a serviceName passed in the journeyConfig" should {
        lazy val insertConfig = insertJourneyConfig(
          journeyId = testJourneyId,
          authInternalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = Some(testCallingServiceName),
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          businessEntity = LimitedCompany
        )
        lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val companyNumberStub = stubRetrieveCompanyProfileFromBE(testJourneyId)(
          status = OK,
          body = Json.toJsObject(testCompanyProfile)
        )
        lazy val ctutrStub = stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)

        lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

        testCheckYourAnswersView(testJourneyId)(result, companyNumberStub, ctutrStub, authStub, insertConfig)
        testServiceName(testCallingServiceName, result, authStub, insertConfig)
      }

      "the applicant does not have a CTUTR" should {
        "return OK" in {
          val insertConfig = insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = RegisteredSociety
          )
          val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          val companyNumberStub = stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))

          lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/check-your-answers-business")
          result.status mustBe OK
        }

        "return a view which" should {
          lazy val insertConfig = insertJourneyConfig(
            journeyId = testJourneyId,
            authInternalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            businessEntity = RegisteredSociety
          )
          lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          lazy val companyNumberStub = stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))

          lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

          testCheckYourAnswersNoCtutrView(testJourneyId)(result, companyNumberStub, authStub, insertConfig)
        }
      }
  }

  "redirect to sign in page" when {
    "the user is not logged in" in {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId,
        continueUrl = testContinueUrl,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        businessEntity = LimitedCompany
      ))
      stubAuthFailure()
      stubRetrieveCompanyProfileFromBE(testJourneyId)(status = OK, body = Json.toJsObject(testCompanyProfile))
      stubRetrieveCtutr(testJourneyId)(status = OK, body = testCtutr)

      lazy val result: WSResponse = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

      result.status mustBe SEE_OTHER
      result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-incorporated-business%2F$testJourneyId%2Fcheck-your-answers-business&origin=incorporated-entity-identification-frontend")
    }
  }

  "return NOT_FOUND" when {
    "the journeyId does not match what is stored in the journey config database" in {
      await(insertJourneyConfig(
        journeyId = testJourneyId + "1",
        authInternalId = testInternalId,
        continueUrl = testContinueUrl,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        businessEntity = LimitedCompany
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

      result.status mustBe NOT_FOUND
    }

    "the auth internal ID does not match what is stored in the journey config database" in {
      await(insertJourneyConfig(
        journeyId = testJourneyId,
        authInternalId = testInternalId + "1",
        continueUrl = testContinueUrl,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        businessEntity = LimitedCompany
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

      result.status mustBe NOT_FOUND
    }

    "neither the journey ID or auth internal ID are found in the journey config database" in {
      await(insertJourneyConfig(
        journeyId = testJourneyId + "1",
        authInternalId = testInternalId + "1",
        continueUrl = testContinueUrl,
        optServiceName = None,
        deskProServiceId = testDeskProServiceId,
        signOutUrl = testSignOutUrl,
        businessEntity = LimitedCompany
      ))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

      result.status mustBe NOT_FOUND
    }
  }

  "throw an Internal Server Exception" when {
    "the user does not have an internal ID" in {
      stubAuth(OK, successfulAuthResponse(None))

      lazy val result = get(s"$baseUrl/$testJourneyId/check-your-answers-business")

      result.status mustBe INTERNAL_SERVER_ERROR
    }
  }
}


"POST /check-your-answers-business" when {
  "the company details are successfully matched" should {
  "return a redirect to the Business Verification Result page" when {
  "the feature switch is enabled" in {
  enable (EnableUnmatchedCtutrJourney)
  await (insertJourneyConfig (
  journeyId = testJourneyId,
  authInternalId = testInternalId,
  continueUrl = testContinueUrl,
  optServiceName = None,
  deskProServiceId = testDeskProServiceId,
  signOutUrl = testSignOutUrl,
  businessEntity = LimitedCompany
  ) )

  stubAuth (OK, successfulAuthResponse (Some (testInternalId) ) )
  stubRetrieveCompanyProfileFromBE (testJourneyId) (status = OK, body = Json.toJsObject (testCompanyProfile) )
  stubRetrieveCtutr (testJourneyId) (status = OK, body = testCtutr)
  stubValidateIncorporatedEntityDetails (testCompanyNumber, testCtutr) (OK, Json.obj ("matched" -> true) )
  stubStoreIdentifiersMatch (testJourneyId) (status = OK)
  stubCreateBusinessVerificationJourney (testCtutr, testJourneyId) (status = CREATED)

  lazy val result = post (s"$baseUrl/$testJourneyId/check-your-answers-business") ()

  result.status mustBe SEE_OTHER
  result.header (LOCATION) mustBe Some (routes.BusinessVerificationController.startBusinessVerificationJourney (testJourneyId).url)
}
}
  "return a redirect to the Business Verification Result page" when {
  "the feature switch is disabled" in {
  disable (EnableUnmatchedCtutrJourney)
  await (insertJourneyConfig (
  journeyId = testJourneyId,
  authInternalId = testInternalId,
  continueUrl = testContinueUrl,
  optServiceName = None,
  deskProServiceId = testDeskProServiceId,
  signOutUrl = testSignOutUrl,
  businessEntity = LimitedCompany
  ) )

  stubAuth (OK, successfulAuthResponse (Some (testInternalId) ) )
  stubRetrieveCompanyProfileFromBE (testJourneyId) (status = OK, body = Json.toJsObject (testCompanyProfile) )
  stubRetrieveCtutr (testJourneyId) (status = OK, body = testCtutr)
  stubValidateIncorporatedEntityDetails (testCompanyNumber, testCtutr) (OK, Json.obj ("matched" -> true) )
  stubStoreIdentifiersMatch (testJourneyId) (status = OK)
  stubCreateBusinessVerificationJourney (testCtutr, testJourneyId) (status = CREATED)

  lazy val result = post (s"$baseUrl/$testJourneyId/check-your-answers-business") ()

  result.status mustBe SEE_OTHER
  result.header (LOCATION) mustBe Some (routes.BusinessVerificationController.startBusinessVerificationJourney (testJourneyId).url)
}
}
}

  "the company details do not match" should {
  "redirect to ctutr mismatch page" when {
  "the feature switch is enabled" in {
  enable (EnableUnmatchedCtutrJourney)
  await (insertJourneyConfig (
  journeyId = testJourneyId,
  authInternalId = testInternalId,
  continueUrl = testContinueUrl,
  optServiceName = None,
  deskProServiceId = testDeskProServiceId,
  signOutUrl = testSignOutUrl,
  businessEntity = LimitedCompany
  ) )

  stubAuth (OK, successfulAuthResponse (Some (testInternalId) ) )
  stubRetrieveCompanyProfileFromBE (testJourneyId) (status = OK, body = Json.toJsObject (testCompanyProfile) )
  stubRetrieveCtutr (testJourneyId) (status = OK, body = testCtutr)
  stubValidateIncorporatedEntityDetails (testCompanyNumber, testCtutr) (OK, Json.obj ("matched" -> false) )
  stubStoreIdentifiersMatch (testJourneyId) (status = OK)

  lazy val result = post (s"$baseUrl/$testJourneyId/check-your-answers-business") ()

  result.status mustBe SEE_OTHER
  result.header (LOCATION) mustBe Some (errorRoutes.CtutrMismatchController.show (testJourneyId).url)

}
}
}
  "redirect to ctutr mismatch page" when {
  "the feature switch is disabled" in {
  disable (EnableUnmatchedCtutrJourney)
  await (insertJourneyConfig (
  journeyId = testJourneyId,
  authInternalId = testInternalId,
  continueUrl = testContinueUrl,
  optServiceName = None,
  deskProServiceId = testDeskProServiceId,
  signOutUrl = testSignOutUrl,
  businessEntity = LimitedCompany
  ) )

  stubAuth (OK, successfulAuthResponse (Some (testInternalId) ) )
  stubRetrieveCompanyProfileFromBE (testJourneyId) (status = OK, body = Json.toJsObject (testCompanyProfile) )
  stubRetrieveCtutr (testJourneyId) (status = OK, body = testCtutr)
  stubValidateIncorporatedEntityDetails (testCompanyNumber, testCtutr) (OK, Json.obj ("matched" -> false) )
  stubStoreIdentifiersMatch (testJourneyId) (status = OK)

  lazy val result = post (s"$baseUrl/$testJourneyId/check-your-answers-business") ()

  result.status mustBe SEE_OTHER
  result.header (LOCATION) mustBe Some (errorRoutes.CtutrMismatchController.show (testJourneyId).url)
}
}

  "the company details do not exist" should {
  "throw an exception" when {
  "the feature switch is disabled" in { //TODO - handle this in the case of entities without corporation tax
  disable (EnableUnmatchedCtutrJourney)
  await (insertJourneyConfig (
  journeyId = testJourneyId,
  authInternalId = testInternalId,
  continueUrl = testContinueUrl,
  optServiceName = None,
  deskProServiceId = testDeskProServiceId,
  signOutUrl = testSignOutUrl,
  businessEntity = LimitedCompany
  ) )

  stubAuth (OK, successfulAuthResponse (Some (testInternalId) ) )
  stubRetrieveCompanyProfileFromBE (testJourneyId) (status = OK, body = Json.toJsObject (testCompanyProfile) )
  stubRetrieveCtutr (testJourneyId) (status = OK, body = testCtutr)

  stubValidateIncorporatedEntityDetails (
  testCompanyNumber,
  testCtutr
  ) (
  status = BAD_REQUEST,
  body = Json.obj (
  "code" -> "NOT_FOUND",
  "reason" -> "The back end has indicated that CT UTR cannot be returned"
  )
  )

  lazy val result = post (s"$baseUrl/$testJourneyId/check-your-answers-business") ()

  result.status mustBe INTERNAL_SERVER_ERROR
}
}
  "redirect to continueUrl" when {
  "feature switch is enabled" in {
  enable (EnableUnmatchedCtutrJourney)
  await (insertJourneyConfig (
  journeyId = testJourneyId,
  authInternalId = testInternalId,
  continueUrl = testContinueUrl,
  optServiceName = None,
  deskProServiceId = testDeskProServiceId,
  signOutUrl = testSignOutUrl,
  businessEntity = LimitedCompany
  ) )

  stubAuth (OK, successfulAuthResponse (Some (testInternalId) ) )
  stubRetrieveCompanyProfileFromBE (testJourneyId) (status = OK, body = Json.toJsObject (testCompanyProfile) )
  stubRetrieveCtutr (testJourneyId) (status = OK, body = testCtutr)
  stubStoreIdentifiersMatch (testJourneyId) (status = OK)
  stubStoreBusinessVerificationStatus (testJourneyId, BusinessVerificationUnchallenged) (status = OK)
  stubStoreRegistrationStatus (testJourneyId, RegistrationNotCalled) (status = OK)

  stubValidateIncorporatedEntityDetails (
  testCompanyNumber,
  testCtutr
  ) (
  status = BAD_REQUEST,
  body = Json.obj (
  "code" -> "NOT_FOUND",
  "reason" -> "The back end has indicated that CT UTR cannot be returned"
  )
  )

  lazy val result = post (s"$baseUrl/$testJourneyId/check-your-answers-business") ()

  result.status mustBe SEE_OTHER
  result.header (LOCATION) mustBe Some (routes.JourneyRedirectController.redirectToContinueUrl (testJourneyId).url)
  verifyStoreBusinessVerificationStatus (testJourneyId, BusinessVerificationUnchallenged)
  verifyStoreRegistrationStatus (testJourneyId, RegistrationNotCalled)
}
}
}
}

}
