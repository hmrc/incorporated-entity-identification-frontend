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


object CaptureCompanyNumberForm {

  val companyNumber = "companyNumber"
  val alphanumericRegex = "^[A-Z0-9]*$"

  def companyNumberEmpty: Constraint[String] = Constraint("company_number.not_entered")(
    companyNumber => validate(
      constraint = companyNumber.isEmpty,
      errMsg = "capture-company-number-empty.error"
    )
  )

  def companyNumberLength: Constraint[String] = Constraint("company_number.min_max_length")(
    companyNumber => validateNot(
      constraint = (companyNumber.length >= 1) && (companyNumber.length <= 8),
      errMsg = "capture-company-number-length.error"
    )
  )

  def companyNumberFormat: Constraint[String] = Constraint("company_number.wrong_format")(
    companyNumber => validateNot(
      constraint = companyNumber.toUpperCase matches alphanumericRegex,
      errMsg = "capture-company-number-format.error"
    )
  )

  val form: Form[String] = {
    Form(
      companyNumber -> text.verifying(
        companyNumberEmpty andThen
          companyNumberLength andThen
          companyNumberFormat
      )

    )


  }
}
