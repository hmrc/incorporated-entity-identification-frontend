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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.WireMockMethods

trait IncorporatedEntityIdentificationStub extends WireMockMethods {

  def stubStoreCompaniesHouseProfile(journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = PUT, uri = s"/journey/$journeyId/companies-house-profile")
      .thenReturn(
        status = status,
        body = body
      )

  def stubRetrieveCompanyName(journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/$journeyId/retrieve-company-name")
      .thenReturn(
        status = status,
        body = body
      )

  def stubStoreCtutr(journeyId: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = PUT, uri = s"/journey/$journeyId/ctutr")
      .thenReturn(
        status = status,
        body = body
      )

  def stubValidateIncorporatedEntityDetails(companyNumber: String, ctutr: String)(status: Int, body: JsObject = Json.obj()): StubMapping = {
    when(method = POST, uri = s"/incorporated-entity-identification/validate-details", body = Json.obj("companyNumber" -> companyNumber, "ctutr" -> ctutr))
      .thenReturn(
        status = status,
        body = body
      )
  }

}
