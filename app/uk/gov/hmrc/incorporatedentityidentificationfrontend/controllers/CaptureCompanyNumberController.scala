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
import play.api.Logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.CaptureCompanyNumberForm
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.capture_company_number_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureCompanyNumberController @Inject()(companyProfileService: CompanyProfileService,
                                               mcc: MessagesControllerComponents,
                                               view: capture_company_number_page,
                                               val authConnector: AuthConnector)
                                              (implicit val config: AppConfig,
                                               ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(
          Ok(view(routes.CaptureCompanyNumberController.submit(journeyId), CaptureCompanyNumberForm.form))
        )
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        CaptureCompanyNumberForm.form.bindFromRequest().fold(
          formWithErrors => Future.successful(
            BadRequest(view(routes.CaptureCompanyNumberController.submit(journeyId), formWithErrors))
          ),
          companyNumber =>
            companyProfileService.retrieveAndStoreCompanyProfile(journeyId, companyNumber).map {
              case Some(_) =>
                Redirect(routes.ConfirmBusinessNameController.show(journeyId))
              case None =>
                throw new InternalServerException(s"Company Profile not found for company number: $companyNumber")
            }
        )
      }
  }

}
