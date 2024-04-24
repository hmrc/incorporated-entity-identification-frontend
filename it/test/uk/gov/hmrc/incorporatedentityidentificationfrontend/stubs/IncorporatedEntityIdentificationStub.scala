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

package test.uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{BusinessVerificationStatus, CompanyProfile, IncorporatedEntityDetailsMatching, RegistrationStatus}
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.{WireMockMethods, WiremockHelper}

trait IncorporatedEntityIdentificationStub extends WireMockMethods {

  def stubStoreCompanyProfile(journeyId: String, companyProfile: CompanyProfile)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/incorporated-entity-identification/journey/$journeyId/companyProfile",
      body = Json.obj(
        "companyName" -> companyProfile.companyName,
        "companyNumber" -> companyProfile.companyNumber,
        "dateOfIncorporation" -> companyProfile.dateOfIncorporation,
        "unsanitisedCHROAddress" -> companyProfile.unsanitisedCHROAddress
      )
    ).thenReturn(
      status = status
    )

  def stubStoreBusinessVerificationStatus(journeyId: String,
                                          businessVerificationStatus: BusinessVerificationStatus
                                         )(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/incorporated-entity-identification/journey/$journeyId/businessVerification",
      body = Json.toJson(businessVerificationStatus)
    ).thenReturn(
      status = status
    )

  def verifyStoreBusinessVerificationStatus(journeyId: String, businessVerificationStatus: BusinessVerificationStatus): Unit = {
    val jsonBody = Json.toJson(businessVerificationStatus)
    WiremockHelper.verifyPut(uri = s"/incorporated-entity-identification/journey/$journeyId/businessVerification", optBody = Some(jsonBody.toString()))
  }

  def stubStoreCtutr(journeyId: String, ctutr: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/incorporated-entity-identification/journey/$journeyId/ctutr", body = JsString(ctutr)
    ).thenReturn(
      status = status
    )

  def stubStoreCHRN(journeyId: String, chrn: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/incorporated-entity-identification/journey/$journeyId/chrn", body = JsString(chrn)
    ).thenReturn(
      status = status
    )

  def stubStoreIdentifiersMatch(journeyId: String, identifiersMatch: IncorporatedEntityDetailsMatching)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/incorporated-entity-identification/journey/$journeyId/identifiersMatch", body = JsString(identifiersMatch.toString)
    ).thenReturn(
      status = status
    )

  def verifyStoreIdentifiersMatch(journeyId: String, identifiersMatch: JsValue): Unit =
    WiremockHelper.verifyPut(
      uri = s"/incorporated-entity-identification/journey/$journeyId/identifiersMatch",
      optBody = Some(Json.stringify(identifiersMatch))
    )

  def stubStoreRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus)(status: Int): StubMapping = {
    when(method = PUT,
      uri = s"/incorporated-entity-identification/journey/$journeyId/registration",
      body = Json.toJsObject(registrationStatus)
    ).thenReturn(
      status = status
    )
  }

  def verifyStoreRegistrationStatus(journeyId: String, registrationStatus: RegistrationStatus): Unit = {
    val jsonBody = Json.toJsObject(registrationStatus)
    WiremockHelper.verifyPut(uri = s"/incorporated-entity-identification/journey/$journeyId/registration", optBody = Some(jsonBody.toString()))
  }

  def stubValidateIncorporatedEntityDetails(companyNumber: String,
                                            ctutr: Option[String]
                                           )(status: Int,
                                             body: JsObject = Json.obj()): StubMapping = {
    when(method = POST,
      uri = s"/incorporated-entity-identification/validate-details",
      body = Json.obj(
        "companyNumber" -> companyNumber,
        "ctutr" -> ctutr
      )
    ).thenReturn(
      status = status,
      body = body
    )
  }

  def stubRetrieveCompanyProfileFromBE(journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/incorporated-entity-identification/journey/$journeyId/companyProfile"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveBusinessVerificationStatus(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/incorporated-entity-identification/journey/$journeyId/businessVerification"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveRegistrationStatus(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/incorporated-entity-identification/journey/$journeyId/registration"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveIncorporatedEntityInformation(journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/incorporated-entity-identification/journey/$journeyId"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveCtutr(journeyId: String)(status: Int, body: String = ""): StubMapping = {
    when(method = GET,
      uri = s"/incorporated-entity-identification/journey/$journeyId/ctutr"
    ).thenReturn(
      status = status,
      body = JsString(body)
    )
  }

  def stubRetrieveChrn(journeyId: String)(status: Int, body: String = ""): StubMapping = {
    when(method = GET,
      uri = s"/incorporated-entity-identification/journey/$journeyId/chrn"
    ).thenReturn(
      status = status,
      body = JsString(body)
    )
  }

  def stubRemoveCtutr(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/incorporated-entity-identification/journey/$journeyId/ctutr"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRemoveCHRN(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/incorporated-entity-identification/journey/$journeyId/chrn"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveIdentifiersMatch(journeyId: String)(status: Int, body: IncorporatedEntityDetailsMatching): StubMapping = {
    when(method = GET,
      uri = s"/incorporated-entity-identification/journey/$journeyId/identifiersMatch"
    ).thenReturn(
      status = status,
      body = JsString(body.toString)
    )
  }

  def stubRemoveAllData(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/incorporated-entity-identification/journey/$journeyId"
    ).thenReturn(
      status = status,
      body = body
    )
}
