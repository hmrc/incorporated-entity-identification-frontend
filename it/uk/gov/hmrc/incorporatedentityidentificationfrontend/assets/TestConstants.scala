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
package uk.gov.hmrc.incorporatedentityidentificationfrontend.assets

import play.api.libs.json.{JsArray, JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessVerificationStatus._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.RegistrationStatus._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID


object TestConstants {

  val testCompanyNumber: String = "12345678"
  val testCompanyNumberInUppercase: String = "CE012345"
  val companyNameKey: String = "companyName"
  val companyNumberKey: String = "companyNumber"
  val testCompanyName: String = "Test Company Ltd"
  val testCHRN: String = "aB12345"
  val testCtutr: String = "1234567890"
  val testJourneyId: String = UUID.randomUUID().toString
  val testSignOutUrl: String = "/signOutUrl"
  val testBusinessVerificationJourneyId = "TestBusinessVerificationJourneyId"
  val testDateOfIncorporation: String = LocalDate.now().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val GGProviderId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testDefaultServiceName: String = "Entity Validation Service"
  val testCallingServiceName: String = "Test Service"
  val testCallingServiceNameFromLabels: String = "Test Service Name from Labels"
  val testContinueUrl: String = "/test"
  val testAccessibilityUrl: String = "/accessibility"
  val testLocalAccessibilityUrl: String = "http://localhost:12346/accessibility-statement/vat-registration"
  val testStagingAccessibilityUrl: String = "https://www.staging.tax.service.gov.uk/accessibility-statement/vat-registration"
  val testSafeId: String = UUID.randomUUID().toString
  val testRegime: String = "VATC"
  val testBusinessVerificationPassJson: JsObject = testBusinessVerificationJson(value = businessVerificationPassKey)
  val testBusinessVerificationFailJson: JsObject = testBusinessVerificationJson(value = businessVerificationFailKey)
  val testSuccessfulRegistration: RegistrationStatus = Registered(testSafeId)
  val testSuccessfulRegJson: JsObject = Json.obj(
    registrationStatusKey -> RegisteredKey,
    registeredBusinessPartnerIdKey -> testSafeId)

  val testSuccessfulRegistrationJson: JsObject = Json.obj("registration" -> testSuccessfulRegJson)

  def testFailedRegJson(failures: JsArray): JsObject = Json.obj(
    registrationStatusKey -> RegistrationFailedKey,
    registrationFailuresKey -> failures)

  def testFailedRegistrationJson(failures: JsArray): JsObject = Json.obj("registration" -> testFailedRegJson(failures))

  val testRegistrationNotCalledJson: JsObject = Json.obj(registrationStatusKey -> RegistrationNotCalledKey)
  val testDeskProServiceId: String = "vrs"
  val IRCTEnrolmentKey = "IR-CT"
  val IRCTReferenceKey = "UTR"
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
  val testCioProfile: CompanyProfile = CompanyProfile(testCompanyName, testCompanyNumber, "", Json.obj())
  private val defaultConfig: PageConfig = PageConfig(None, testDeskProServiceId, testSignOutUrl, testAccessibilityUrl, None)
  val testLimitedCompanyJourneyConfig: JourneyConfig = createTestJourneyConfig(LimitedCompany)
  val testLimitedCompanyJourneyConfigWithServiceName: JourneyConfig = createTestJourneyConfig(LimitedCompany)
    .copy(pageConfig = defaultConfig.copy(optServiceName = Some("Entity Validation Service")))
  val testRegisteredSocietyJourneyConfig: JourneyConfig = createTestJourneyConfig(RegisteredSociety)
  val testRegisteredSocietyJourneyConfigWithServiceName: JourneyConfig = createTestJourneyConfig(RegisteredSociety)
    .copy(pageConfig = defaultConfig.copy(optServiceName = Some("Entity Validation Service")))
  val testCharitableIncorporatedOrganisationJourneyConfig: JourneyConfig = createTestJourneyConfig(CharitableIncorporatedOrganisation)
  val testRegistrationFailure: Array[Failure] = Array(Failure("PARTY_TYPE_MISMATCH", "The remote endpoint has indicated there is Party Type mismatch"))
  val testFailedRegistration: RegistrationStatus = RegistrationFailed(Some(testRegistrationFailure))
  val testMultipleRegistrationFailure: Array[Failure] = Array(Failure("INVALID_REGIME", "Request has not passed validation.  Invalid regime"),
    Failure("INVALID_PAYLOAD", "Request has not passed validation. Invalid payload."))
  val testWelshServiceName: String = "Welsh service name"
  val testEnglishServiceName: String = "English service name"

  val testDefaultWelshJourneyConfig: JourneyConfig =
    JourneyConfig(
      continueUrl = testContinueUrl,
      pageConfig = PageConfig(
        optServiceName = None,
        deskProServiceId= testDeskProServiceId,
        signOutUrl= testSignOutUrl,
        accessibilityUrl=testAccessibilityUrl,
        optLabels = Some(JourneyLabels(optWelshServiceName = Some(testWelshServiceName), None))
      ),
      businessEntity = RegisteredSociety,
      businessVerificationCheck = true,
      regime = testRegime
    )

  val testDefaultWelshServiceName: String = "Gwasanaeth Dilysu Endid"

  private def createTestJourneyConfig(entityType: BusinessEntity): JourneyConfig =
    JourneyConfig(
      testContinueUrl,
      defaultConfig,
      entityType,
      businessVerificationCheck = true,
      testRegime)

  val testIncorporatedEntityFullJourneyDataJson: JsObject = {
    Json.obj(
      "companyProfile" -> testCompanyProfile,
      "ctutr" -> testCtutr,
      "identifiersMatch" -> true,
      "businessVerification" -> testBusinessVerificationPassJson,
      "registration" -> testSuccessfulRegistrationJson
    )
  }

  def testRegisterAuditEventJson(companyNumber: String,
                                 isMatch: String,
                                 ctUtr: String,
                                 verificationStatus: String,
                                 registrationStatus: String): JsObject = {
    Json.obj(
      "callingService" -> "Entity Validation Service",
      "businessType" -> "UK Company",
      "companyNumber" -> companyNumber,
      "isMatch" -> isMatch,
      "CTUTR" -> ctUtr,
      "VerificationStatus" -> verificationStatus,
      "RegisterApiStatus" -> registrationStatus
    )
  }

  val testJourneyConfigWithoutAccessibilityUrlAsJsObject: JsObject =
    Json.obj(
      "continueUrl" -> testContinueUrl,
      "pageConfig" -> Json.obj(
        "deskProServiceId" -> testDeskProServiceId,
        "signOutUrl" -> testSignOutUrl
      ),
      "businessEntity" -> "LtdCompany",
      "businessVerificationCheck" -> true,
      "regime" -> testRegime
    )

  val testJourneyConfigWithDefaultAccessibilityUrl: JourneyConfig =
    JourneyConfig(
      continueUrl = testContinueUrl,
      pageConfig = PageConfig(
        None,
        testDeskProServiceId,
        testSignOutUrl,
        testAccessibilityUrl,
        None
      ),
      businessEntity = LimitedCompany,
      businessVerificationCheck = true,
      regime = testRegime
    )

  def testBusinessVerificationJson(value: String): JsObject = Json.obj(businessVerificationStatusKey -> value)

  def testDefaultIncorporatedEntityInformation(businessVerificationStatus: BusinessVerificationStatus): IncorporatedEntityInformation =
    IncorporatedEntityInformation(
      companyProfile = testCompanyProfile,
      optCtutr = Some(testCtutr),
      optChrn = Some(testCHRN),
      identifiersMatch = DetailsMatched,
      businessVerification = Some(businessVerificationStatus),
      registration = testSuccessfulRegistration
    )

  def testBVCreationPostData(ctUtr: String, journeyId: String): JsObject = Json.obj(
    "journeyType" -> "BUSINESS_VERIFICATION",
    "origin" -> "vatc",
    "identifiers" -> Json.arr(
      Json.obj(
        "ctUtr" -> ctUtr
      )
    ),
    "continueUrl" -> s"/identify-your-incorporated-business/$journeyId/business-verification-result",
    "accessibilityStatementUrl" -> "/accessibility",
    "deskproServiceName" -> "vrs",
    "pageTitle" -> "Entity Validation Service"

  )

  val optLabelsAsString: String =
    s"""{
       |  "labels" : {
       |                "cy" : {
       |                         "optServiceName" : "$testWelshServiceName"
       |                       },
       |                "en" : {
       |                         "optServiceName" : "$testEnglishServiceName"
       |                       }
       |             }
       |}""".stripMargin

  val optLabelsAsJson: JsObject = Json.parse(optLabelsAsString).as[JsObject]

  val optLabels: JourneyLabels = JourneyLabels(Some(testWelshServiceName), Some(testEnglishServiceName))
}
