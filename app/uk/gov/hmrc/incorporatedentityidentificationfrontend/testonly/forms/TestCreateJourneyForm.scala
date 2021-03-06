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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.forms

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.validation.Constraint
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.MappingUtil.optText
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.ValidationHelper._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{BusinessEntity, LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{JourneyConfig, PageConfig}


object TestCreateJourneyForm {

  val continueUrl = "continueUrl"
  val serviceName = "serviceName"
  val deskProServiceId = "deskProServiceId"
  val alphanumericRegex = "^[A-Z0-9]*$"
  val signOutUrl = "signOutUrl"
  val entityType = "entityType"

  def continueUrlEmpty: Constraint[String] = Constraint("continue_url.not_entered")(
    companyNumber => validate(
      constraint = companyNumber.isEmpty,
      errMsg = "Continue URL not entered"
    )
  )

  def deskProServiceIdEmpty: Constraint[String] = Constraint("desk_pro_service_id.not_entered")(
    serviceId => validate(
      constraint = serviceId.isEmpty,
      errMsg = "DeskPro Service Identifier is not entered"
    )
  )

  def signOutUrlEmpty: Constraint[String] = Constraint("sign_out_url.not_entered")(
    signOutUrl => validate(
      constraint = signOutUrl.isEmpty,
      errMsg = "Sign Out Url is not entered"
    )
  )

  val LtdCompanyKey = "ltdCompany"
  val RegisteredSocietyKey = "registeredSociety"

  def entityTypeFormatter: Formatter[BusinessEntity] = new Formatter[BusinessEntity] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BusinessEntity] =
      data.get(key) match {
        case Some(LtdCompanyKey) => Right(LimitedCompany)
        case Some(RegisteredSocietyKey) => Right(LimitedCompany)
        case _ => Left(Seq(FormError(key, "Invalid entity type")))
      }

    override def unbind(key: String, value: BusinessEntity): Map[String, String] = value match {
      case LimitedCompany => Map(key -> LtdCompanyKey)
      case RegisteredSociety => Map(key -> RegisteredSocietyKey)
    }
  }

  def form(businessEntity: BusinessEntity): Form[JourneyConfig] = {
    Form(mapping(
      continueUrl -> text.verifying(continueUrlEmpty),
      serviceName -> optText,
      deskProServiceId -> text.verifying(deskProServiceIdEmpty),
      signOutUrl -> text.verifying(signOutUrlEmpty)
    )((continueUrl, serviceName, deskProServiceId, signOutUrl) =>
      JourneyConfig.apply(continueUrl, PageConfig(serviceName, deskProServiceId, signOutUrl), businessEntity)
    )(journeyConfig =>
      Some(journeyConfig.continueUrl, journeyConfig.pageConfig.optServiceName,
        journeyConfig.pageConfig.deskProServiceId, journeyConfig.pageConfig.signOutUrl
      )))
  }

}
