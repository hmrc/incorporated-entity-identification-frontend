/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.errorpages

import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.{routes => appRoutes}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.JourneyService
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.errorpages.ctutr_mismatch_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CtutrMismatchController @Inject()(journeyService: JourneyService,
                                        mcc: MessagesControllerComponents,
                                        view: ctutr_mismatch_page,
                                        val authConnector: AuthConnector,
                                        messagesHelper: MessagesHelper
                                       )(implicit val config: AppConfig,
                                         executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).map {
            journeyConfig =>
              implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
              Ok(view(journeyConfig.pageConfig, routes.CtutrMismatchController.tryAgain(journeyId), journeyId))
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def tryAgain(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(Redirect(appRoutes.CaptureCompanyNumberController.show(journeyId)))
      }
  }

}
