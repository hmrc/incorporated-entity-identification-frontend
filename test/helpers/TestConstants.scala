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
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{BusinessEntity, LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID


object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testAuthInternalId: String = UUID.randomUUID().toString
  val testServiceName: String = "Test Service"
  val defaultServiceName: String = "Entity Validation Service"
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
      Some(testCtutr),
      testIdentifiersMatch,
      Some(testPassedBusinessVerificationStatus),
      Registered(testSafeId)
    )
  val testContinueUrl = "/test"
  val testSignOutUrl = "/signOutUrl"
  val testCharityNumber = "CE123456"

  def testJourneyConfigLimitedCompany(): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl),
    businessEntity = LimitedCompany,
    businessVerificationCheck = true
  )
  def testJourneyConfigLimitedCompanyWithoutBV(): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl),
    businessEntity = LimitedCompany,
    businessVerificationCheck = false
  )

  def testJourneyConfigRegisteredSociety(): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl),
    businessEntity = RegisteredSociety,
    businessVerificationCheck = true
  )

  def testJourneyConfigRegisteredSocietyWithoutBV(): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl),
    businessEntity = RegisteredSociety,
    businessVerificationCheck = false
  )
  val testUkCompanySuccessfulAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "UK Company",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> true,
    "VerificationStatus" -> testPassedBusinessVerificationStatus,
    "RegisterApiStatus" -> Registered(testSafeId),
    "CTUTR" -> testCtutr
  )

  val testDetailsNotFoundAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "UK Company",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "VerificationStatus" -> testUnchallengedBusinessVerificationStatus,
    "RegisterApiStatus" -> RegistrationNotCalled,
    "CTUTR" -> testCtutr
  )

  val testDetailsUtrMismatchAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "UK Company",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "VerificationStatus" -> testFailedBusinessVerificationStatus,
    "RegisterApiStatus" -> RegistrationNotCalled
  )

  val testRegisteredSocietyAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "Registered Society",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> true,
    "VerificationStatus" -> testPassedBusinessVerificationStatus,
    "RegisterApiStatus" -> Registered(testSafeId),
    "CTUTR" -> testCtutr
  )

  val testDetailsNotFoundRegisteredSocietyAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "Registered Society",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "VerificationStatus" -> testUnchallengedBusinessVerificationStatus,
    "RegisterApiStatus" -> RegistrationNotCalled,
    "CTUTR" -> testCtutr
  )

  val testDetailsUtrMismatchRegisteredSocietyAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "Registered Society",
    "companyNumber" -> testCompanyProfile.companyNumber,
    "isMatch" -> false,
    "VerificationStatus" -> testFailedBusinessVerificationStatus,
    "RegisterApiStatus" -> RegistrationNotCalled
  )

  val testCIOAuditEventJson: JsObject = Json.obj(
    "callingService" -> defaultServiceName,
    "businessType" -> "CIO",
    "companyNumber" -> testCharityNumber,
    "identifiersMatch" -> false,
    "VerificationStatus" -> testUnchallengedBusinessVerificationStatus,
    "RegisterApiStatus" -> RegistrationNotCalled
  )

  val testIrCtEnrolment: Enrolment = Enrolment("IR-CT", Seq(EnrolmentIdentifier("UTR", testCtutr)), "Activated", None)
}
