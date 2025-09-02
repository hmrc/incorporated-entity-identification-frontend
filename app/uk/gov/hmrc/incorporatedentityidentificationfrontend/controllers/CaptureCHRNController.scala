/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.CaptureCHRNForm
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.MessagesHelper
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.capture_chrn_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureCHRNController @Inject()(storageService: StorageService,
                                      journeyService: JourneyService,
                                      mcc: MessagesControllerComponents,
                                      view: capture_chrn_page,
                                      messagesHelper: MessagesHelper,
                                      val authConnector: AuthConnector)
                                     (implicit val config: AppConfig,
                                      ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).map {
            journeyConfig =>
              val remoteMessagesApi = messagesHelper.getRemoteMessagesApi(journeyConfig)
              implicit val messages: Messages = remoteMessagesApi.preferred(request)
              Ok(view(
                journeyId = journeyId,
                pageConfig = journeyConfig.pageConfig,
                formAction = routes.CaptureCHRNController.submit(journeyId),
                form = CaptureCHRNForm.form
              ))
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          CaptureCHRNForm.form.bindFromRequest().fold(
            formWithErrors =>
              journeyService.getJourneyConfig(journeyId, authInternalId).map {
                journeyConfig =>
                  implicit val messages: Messages = messagesHelper.getRemoteMessagesApi(journeyConfig).preferred(request)
                  BadRequest(view(
                    journeyId = journeyId,
                    pageConfig = journeyConfig.pageConfig,
                    formAction = routes.CaptureCHRNController.submit(journeyId),
                    form = formWithErrors
                  ))
              },
            chrn =>
              storageService.storeCHRN(journeyId, chrn).map {
                _ =>
                  Redirect(routes.CheckYourAnswersController.show(journeyId))
              }
          )
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def noChrn(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          for {
            _ <- journeyService.getJourneyConfig(journeyId, authInternalId)
            _ <- storageService.removeCHRN(journeyId)
          } yield
            Redirect(routes.CheckYourAnswersController.show(journeyId))
        case _ =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

}
