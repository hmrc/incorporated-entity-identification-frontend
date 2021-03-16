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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.stubs.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, InjectedController}

import javax.inject.Singleton
import scala.concurrent.Future

@Singleton
class CompaniesHouseStubController extends InjectedController {

  private val companyNameKey = "company_name"
  private val companyNumberKey = "company_number"
  private val dateOfIncorporationKey = "date_of_creation"
  private val stubCompanyName = "Test Company Ltd"
  private val stubDateOfIncorporation = "2020-01-01"
  private val registeredOfficeAddressKey = "registered_office_address"

  def getCompanyInformation(companyNumber: String): Action[AnyContent] = Action.async {

    Future.successful(companyNumber match {
      case "00000001" => NotFound
      case _ => Ok(Json.obj(
        companyNameKey -> stubCompanyName,
        companyNumberKey -> companyNumber,
        dateOfIncorporationKey -> stubDateOfIncorporation,
        registeredOfficeAddressKey -> Json.obj(
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
      ))
    })

  }

}
