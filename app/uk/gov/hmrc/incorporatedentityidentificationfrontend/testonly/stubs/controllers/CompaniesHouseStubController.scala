/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, InjectedController}

import javax.inject.{Inject, Singleton}
import scala.util.matching.Regex

@Singleton
class CompaniesHouseStubController @Inject()(override val controllerComponents: MessagesControllerComponents) extends InjectedController {
  val CharitableIncorporatedOrganisation: Regex = "CE(.*)".r
  private val companyNameKey = "company_name"
  private val companyNumberKey = "company_number"
  private val dateOfIncorporationKey = "date_of_creation"
  private val stubCompanyName = "Test Company Ltd"
  private val stubLongCompanyName = "Test Long Company name Test Long Company name Test Long Company name Test Long Company name Test Long Company name"
  private val stubCharityName = "Test Charity"
  private val stubDateOfIncorporation = "2020-01-01"
  private val stubOldDateOfIncorporation = "2017-01-01"
  private val registeredOfficeAddressKey = "registered_office_address"
  private val stubRegisteredOfficeAddress = Json.obj(
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
  private val stubRegisteredOfficeAddressOverseas = Json.obj(
    "address_line_1" -> "Overseas Line 1",
    "address_line_2" -> "Overseas Line 2",
    "care_of" -> "test name",
    "country" -> "Ireland",
    "locality" -> "Overseas City",
    "po_box" -> "123",
    "premises" -> "1",
    "region" -> "Overseas Region"
  )

  def getCompanyInformation(companyNumber: String): Action[AnyContent] = {

    val stubLtdCompanyProfile = Json.obj(
      companyNameKey -> stubCompanyName,
      companyNumberKey -> companyNumber,
      dateOfIncorporationKey -> stubDateOfIncorporation,
      registeredOfficeAddressKey -> stubRegisteredOfficeAddress
    )
    val stubLtdCompanyProfileWithOldIncorporationDate = Json.obj(
      companyNameKey -> stubCompanyName,
      companyNumberKey -> companyNumber,
      dateOfIncorporationKey -> stubOldDateOfIncorporation,
      registeredOfficeAddressKey -> stubRegisteredOfficeAddress
    )

    val stubLtdCompanyProfileOverseas = Json.obj(
      companyNameKey -> "Overseas LTD",
      companyNumberKey -> companyNumber,
      dateOfIncorporationKey -> stubOldDateOfIncorporation,
      registeredOfficeAddressKey -> stubRegisteredOfficeAddressOverseas
    )

    val stubLtdCompanyProfileWithLongCompanyName = Json.obj(
      companyNameKey -> stubLongCompanyName,
      companyNumberKey -> companyNumber,
      dateOfIncorporationKey -> stubDateOfIncorporation,
      registeredOfficeAddressKey -> stubRegisteredOfficeAddress
    )

    val stubCioProfile = Json.obj(
      companyNameKey -> stubCharityName,
      companyNumberKey -> companyNumber,
      dateOfIncorporationKey -> "",
      registeredOfficeAddressKey -> Json.obj()
    )

    Action {
      companyNumber match {
        case "12383404" => Ok(Json.toJson(validCompanyProfile(companyNumber, companyNameSuffix = 4, address_line_1 = "10 HIGH STREET")))
        case "11132304" => Ok(Json.toJson(validCompanyProfile(companyNumber, companyNameSuffix = 2, address_line_1 = "20 TEST DRIVE")))
        case "12404104" => Ok(Json.toJson(validCompanyProfile(companyNumber, companyNameSuffix = 5, address_line_1 = "30 SPRING ROAD")))
        case "12423604" => Ok(Json.toJson(validCompanyProfile(companyNumber, companyNameSuffix = 6, address_line_1 = "40 SUMMER ROW")))
        case "12444304" => Ok(Json.toJson(validCompanyProfile(companyNumber, companyNameSuffix = 7, address_line_1 = "50 PAIGNTON PLACE")))
        case "12463604" => Ok(Json.toJson(validCompanyProfile(companyNumber, companyNameSuffix = 8, address_line_1 = "60 ASTON AVENUE")))
        case "12482404" => Ok(Json.toJson(validCompanyProfile(companyNumber, companyNameSuffix = 9, address_line_1 = "70 AUTUMN TERRACE")))
        case "12502004" => Ok(Json.toJson(validCompanyProfile(companyNumber, companyNameSuffix = 10, address_line_1 = "80 GERRARD WAY").copy(company_name = "VAT PENALTY 10")))
        case "00000001" => NotFound
        case "00000002" => Ok(stubLtdCompanyProfileWithOldIncorporationDate)
        case "00000003" => Ok(stubLtdCompanyProfileOverseas)
        case "00000004" => Ok(stubLtdCompanyProfileWithLongCompanyName)
        case CharitableIncorporatedOrganisation(_) => Ok(stubCioProfile)
        case _ => Ok(stubLtdCompanyProfile)
      }
    }
  }

  private def validCompanyProfile(companyNumber: String, companyNameSuffix: Int, address_line_1: String): CompanyProfile = {
    val registeredOfficeAddressReceivedByIncorporationInformation = RegisteredOfficeAddress(
      address_line_1 = "10 HIGH STREET",
      address_line_2 = "TELFORD",
      country = "United Kingdom",
      locality = "SHROPSHIRE",
      postal_code = "TF3 4ER",
      premises = "1"
    )

    CompanyProfile(
      company_name = "VAT PENALTY REFORM " + companyNameSuffix,
      company_number = companyNumber,
      date_of_creation = "2020-01-01",
      registered_office_address = registeredOfficeAddressReceivedByIncorporationInformation.copy(address_line_1 = address_line_1)
    )
  }

  case class RegisteredOfficeAddress(address_line_1: String,
                                     address_line_2: String,
                                     country: String,
                                     locality: String,
                                     postal_code: String,
                                     premises: String)

  case class CompanyProfile(company_name: String,
                            company_number: String,
                            date_of_creation: String,
                            registered_office_address: RegisteredOfficeAddress)

  implicit val registeredOfficeAddressWriter: OWrites[RegisteredOfficeAddress] = Json.writes[RegisteredOfficeAddress]

  implicit val companyProfileWriter: OWrites[CompanyProfile] = Json.writes[CompanyProfile]

}
