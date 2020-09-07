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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.JourneyService
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.check_your_answers_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(journeyService: JourneyService,
                                           mcc: MessagesControllerComponents,
                                           view: check_your_answers_page)
                                          (implicit val config: AppConfig,
                                           executionContext: ExecutionContext) extends FrontendController(mcc) {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      val companyNumber = "12345678"
      val ctutr = "1234567890"

      Future.successful(
        Ok(view(routes.CheckYourAnswersController.submit(journeyId), ctutr, companyNumber, journeyId))
      )
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      journeyService.getJourneyConfig(journeyId).map(
        journeyConfig => SeeOther(journeyConfig.continueUrl)
      )
  }
}
