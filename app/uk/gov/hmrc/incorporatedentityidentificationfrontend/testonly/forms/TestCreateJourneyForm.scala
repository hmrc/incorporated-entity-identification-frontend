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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.testonly.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.MappingUtil.optText
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.utils.ValidationHelper._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig


object TestCreateJourneyForm {

  val continueUrl = "continueUrl"
  val serviceName = "serviceName"
  val alphanumericRegex = "^[A-Z0-9]*$"

  def continueUrlEmpty: Constraint[String] = Constraint("continue_url.not_entered")(
    companyNumber => validate(
      constraint = companyNumber.isEmpty,
      errMsg = "Continue URL not entered"
    )
  )

  val form: Form[JourneyConfig] = {
    Form(mapping(
      continueUrl -> text.verifying(continueUrlEmpty),
      serviceName -> optText
    )(JourneyConfig.apply)(JourneyConfig.unapply))
  }

}
