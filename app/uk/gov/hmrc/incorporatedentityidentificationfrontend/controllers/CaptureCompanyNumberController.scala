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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.CaptureCompanyNumberForm
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.capture_company_number_page
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class CaptureCompanyNumberController @Inject()(mcc: MessagesControllerComponents,
                                               view: capture_company_number_page)
                                              (implicit val config: AppConfig) extends FrontendController(mcc) {

  def show: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(view(routes.CaptureCompanyNumberController.submit(), CaptureCompanyNumberForm.form)))
  }

  def submit: Action[AnyContent] = Action {
    implicit request => NotImplemented
  }


}
