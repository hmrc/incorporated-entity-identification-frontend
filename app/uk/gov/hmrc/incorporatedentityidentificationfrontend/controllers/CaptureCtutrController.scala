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
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.CaptureCtutrForm
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.{IncorporatedEntityInformationService, JourneyService}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.capture_ctutr_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class CaptureCtutrController @Inject()(mcc: MessagesControllerComponents,
                                       view: capture_ctutr_page,
                                       incorporatedEntityInformationService: IncorporatedEntityInformationService,
                                       journeyService: JourneyService,
                                       val authConnector: AuthConnector
                                      )(implicit val config: AppConfig,
                                        executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        val getServiceName = journeyService.getJourneyConfig(journeyId).map {
          _.pageConfig.optServiceName.getOrElse(config.defaultServiceName)
        }

        getServiceName.map {
          serviceName =>
            Ok(view(serviceName, routes.CaptureCtutrController.submit(journeyId), CaptureCtutrForm.form))
        }
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        CaptureCtutrForm.form.bindFromRequest().fold(
          formWithErrors => {
            val getServiceName = journeyService.getJourneyConfig(journeyId).map {
              _.pageConfig.optServiceName.getOrElse(config.defaultServiceName)
            }

            getServiceName.map {
              serviceName =>
                BadRequest(view(serviceName, routes.CaptureCtutrController.submit(journeyId), formWithErrors))
            }
          },
          ctutr =>
            incorporatedEntityInformationService.storeCtutr(journeyId, ctutr).map {
              _ => Redirect(routes.CheckYourAnswersController.show(journeyId))
            }

        )
      }
  }

}
