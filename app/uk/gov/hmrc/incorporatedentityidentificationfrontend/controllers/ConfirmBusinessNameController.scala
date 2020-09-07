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
import play.api.mvc._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.CompanyNameStorageService
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.confirm_business_name_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class ConfirmBusinessNameController @Inject()(companyNameStorageService: CompanyNameStorageService,
                                              mcc: MessagesControllerComponents,
                                              view: confirm_business_name_page
                                             )(implicit val config: AppConfig,
                                               executionContext: ExecutionContext) extends FrontendController(mcc) {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      companyNameStorageService.retrieveCompanyName(journeyId).map {
        companyName =>
          Ok(view(routes.ConfirmBusinessNameController.submit(journeyId), companyName, journeyId))
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      val companyName = "Test Company Ltd" // TODO change when backend API is implemented
      companyNameStorageService.storeCompanyName(journeyId, companyName).map {
        _ =>
          Redirect(routes.CaptureCtutrController.show(journeyId))
      }
  }
}
