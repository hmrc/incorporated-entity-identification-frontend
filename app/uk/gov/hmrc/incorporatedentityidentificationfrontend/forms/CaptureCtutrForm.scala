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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.ConstraintUtil._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.ValidationHelper._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{BusinessEntity, LimitedCompany, RegisteredSociety}

import scala.util.matching.Regex

object CaptureCtutrForm {

  val ctutrRegex: Regex = "[0-9]{10}".r
  val ctutr = "ctutr"

  def noCtutrEntered(businessEntity: BusinessEntity): Constraint[String] = Constraint("ctutr_not_entered")(
    ctutr => validate(
      constraint = ctutr.isEmpty,
      errMsg = businessEntity match {
        case LimitedCompany => "capture-ctutr.error.limited_company_no-entry"
        case RegisteredSociety => "capture-ctutr.error.registered_society_no-entry"
      }
    )
  )

  def ctutrInvalid(businessEntity: BusinessEntity): Constraint[String] = Constraint("ctutr_invalid")(
    ctutr => validateNot(
      constraint = ctutr.matches(ctutrRegex.regex),
      errMsg = businessEntity match {
        case LimitedCompany => "capture-ctutr.error.limited_company_incorrect-format"
        case RegisteredSociety => "capture-ctutr.error.registered_society_incorrect-format"
      }
    )
  )

  def ctutrInvalidLength(businessEntity: BusinessEntity): Constraint[String] = Constraint("ctutr.invalid_length")(
    ctutr => validate(
      constraint = ctutr.length != 10,
      errMsg = businessEntity match {
        case LimitedCompany => "capture-ctutr.error.limited_company_incorrect-format"
        case RegisteredSociety => "capture-ctutr.error.registered_society_incorrect-format"
      }
    )
  )

  def form(businessEntity: BusinessEntity): Form[String] =
    Form(
      "ctutr" -> text.verifying(noCtutrEntered(businessEntity) andThen ctutrInvalid(businessEntity) andThen ctutrInvalidLength(businessEntity))
    )

}
