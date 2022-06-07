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
package uk.gov.hmrc.incorporatedentityidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.{WireMockMethods, WiremockHelper}

trait RegisterStub extends WireMockMethods {

  private def jsonBody(crn: String, ctutr: String, regime: String): JsObject = {
    Json.obj(
      "crn" -> crn,
      "ctutr" -> ctutr,
    "regime" -> regime)
  }

  def stubLimitedCompanyRegister(crn: String, ctutr: String, regime: String)(status: Int, body: JsObject): StubMapping = {
    when(method = POST, uri = "/incorporated-entity-identification/register-limited-company", jsonBody(crn, ctutr, regime))
      .thenReturn(
        status = status,
        body = body
      )
  }

  def verifyLimitedCompanyRegister(crn: String, ctutr: String, regime: String): Unit = {
    WiremockHelper.verifyPost(uri = "/incorporated-entity-identification/register-limited-company", optBody = Some(jsonBody(crn, ctutr, regime).toString()))
  }

  def stubRegisteredSocietyRegister(crn: String, ctutr: String, regime: String)(status: Int, body: JsObject): StubMapping = {
    when(method = POST, uri = "/incorporated-entity-identification/register-registered-society", jsonBody(crn, ctutr, regime))
      .thenReturn(
        status = status,
        body = body
      )
  }

  def verifyRegisteredSocietyRegister(crn: String, ctutr: String, regime: String): Unit = {
    WiremockHelper.verifyPost(uri = "/incorporated-entity-identification/register-registered-society", optBody = Some(jsonBody(crn, ctutr, regime).toString()))
  }

  def verifyRegisterAudit(): Unit = {
    WiremockHelper.verifyPost(uri = "/write/audit", optBody = Some("Audit"))
//    WiremockHelper.verifyPost(uri = "/write/audit/merged")
//    WiremockHelper.verifyPost(uri = "/write/audit")
  }
}
