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
import play.api.libs.json.{JsObject, Json}
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.WireMockMethods

trait CompaniesHouseApiStub extends WireMockMethods {

  def stubRetrieveCompanyProfileFromCoHo(companyNumber: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/incorporation-information/$companyNumber/incorporated-company-profile")
      .thenReturn(
        status = status,
        body = body
      )

  def stubRetrieveCompanyProfileFromStub(companyNumber: String)(status: Int, body: JsObject = Json.obj()): StubMapping =
    when(method = GET, uri = s"/identify-your-incorporated-business/test-only/$companyNumber/incorporated-company-profile")
      .thenReturn(
        status = status,
        body = body
      )

  def companyProfileJson(companyNumber: String, companyName: String, dateOfIncorporation: Option[String], address: JsObject): JsObject = {

    val companyNameKey = "company_name"
    val companyNumberKey = "company_number"
    val dateOfIncorporationKey = "date_of_creation"
    val registeredOfficeAddressKey = "registered_office_address"

    Json.obj(
      companyNameKey -> companyName,
      companyNumberKey -> companyNumber,
      dateOfIncorporationKey -> dateOfIncorporation,
      registeredOfficeAddressKey -> address
    )

  }

}
