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

import scala.util.matching.Regex


object CaptureCHRNForm {

  val chrnKey: String = "chrn"
  val chrnRegex: Regex = "[A-Za-z]{1,2}[0-9]{1,5}".r

  def chrnNumberEmpty: Constraint[String] = Constraint("chrn.not_entered")(
    chrn => validate(
      constraint = chrn.isEmpty,
      errMsg = "chrn.error.no-entry"
    )
  )

  def chrnInvalidLength: Constraint[String] = Constraint("chrn.min_max_length")(
    chrn => validateNot(
      constraint = chrn.nonEmpty && (chrn.length <= 7),
      errMsg = "chrn.error.invalid-length"
    )
  )

  def chrnInvalidCharacters: Constraint[String] = Constraint("chrn.wrong_format")(
    chrn => validateNot(
      constraint = chrn matches chrnRegex.regex,
      errMsg = "chrn.error.invalid-format"
    )
  )

  val form: Form[String] = {
    Form(
      chrnKey -> text.verifying(
        chrnNumberEmpty andThen chrnInvalidLength andThen chrnInvalidCharacters
      )
    )

  }
}
