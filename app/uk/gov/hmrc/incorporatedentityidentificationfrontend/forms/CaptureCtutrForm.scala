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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.ValidationHelper._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.ConstraintUtil._

import scala.util.matching.Regex

object CaptureCtutrForm {

  val ctutrRegex: Regex = "[0-9]{10}".r
  val ctutr = "ctutr"

  private val noCtutrEntered: Constraint[String] = Constraint("ct-utr_not_entered")(
    ctutr => validate(
      constraint = ctutr.isEmpty,
      errMsg = "ct-utr.error.no-entry"
    )
  )

  private val ctutrInvalid: Constraint[String] = Constraint("ct-utr_invalid")(
    ctutr => validateNot(
      constraint = ctutr.matches(ctutrRegex.regex),
      errMsg = "ct-utr.error.incorrect-format"
    )
  )

  val form: Form[String] =
    Form(
      "ctutr" -> text.verifying(noCtutrEntered andThen ctutrInvalid)
    )

}
