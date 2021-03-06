/*
 * Copyright 2021 HM Revenue & Customs
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

package helpers

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationPass, _}

import java.time.LocalDate
import java.util.UUID


object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testAuthInternalId: String = UUID.randomUUID().toString
  val testCompanyNumber: String = "12345678"
  val testCtutr: String = "1234567890"
  val testDateOfIncorporation: String = LocalDate.now().toString
  val testCompanyName: String = "ABC Limited"
  val testIdentifiersMatch: Boolean = true
  val testAddress: JsObject = Json.obj(
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
  val testCompanyProfile: CompanyProfile = CompanyProfile(testCompanyName, testCompanyNumber, testDateOfIncorporation, testAddress)
  val testSafeId: String = UUID.randomUUID().toString
  val testPassedBusinessVerificationStatus: BusinessVerificationStatus = BusinessVerificationPass
  val testFailedBusinessVerificationStatus: BusinessVerificationStatus = BusinessVerificationFail
  val testUnchallengedBusinessVerificationStatus: BusinessVerificationStatus = BusinessVerificationUnchallenged
  val testCtEnrolledStatus: BusinessVerificationStatus = CtEnrolled
  val testIncorporatedEntityInformation: IncorporatedEntityInformation =
    IncorporatedEntityInformation(
      testCompanyProfile,
      testCtutr,
      testIdentifiersMatch,
      testPassedBusinessVerificationStatus,
      Registered(testSafeId)
    )
  val testContinueUrl = "/test"
  val testSignOutUrl = "/signOutUrl"
}
