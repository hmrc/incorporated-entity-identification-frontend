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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers

import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.CaptureCtutrForm
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{LimitedCompany, RegisteredSociety}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.{StorageService, JourneyService}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.{capture_ctutr_page, capture_optional_ctutr_page}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureCtutrController @Inject()(mcc: MessagesControllerComponents,
                                       ctutr_view: capture_ctutr_page,
                                       optional_ctutr_view: capture_optional_ctutr_page,
                                       storageService: StorageService,
                                       journeyService: JourneyService,
                                       val authConnector: AuthConnector
                                      )(implicit val config: AppConfig,
                                        ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).map {
            journeyConfig =>
              journeyConfig.businessEntity match {
                case LimitedCompany =>
                  Ok(ctutr_view(journeyConfig.pageConfig, routes.CaptureCtutrController.submit(journeyId), CaptureCtutrForm.form))
                case RegisteredSociety =>
                  Ok(optional_ctutr_view(journeyId, journeyConfig.pageConfig, routes.CaptureCtutrController.submit(journeyId), CaptureCtutrForm.form))
                case invalidEntity =>
                  throw new InternalServerException(s"Invalid entity: $invalidEntity on CTUTR page")
              }
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          CaptureCtutrForm.form.bindFromRequest().fold(
            formWithErrors => {
              journeyService.getJourneyConfig(journeyId, authInternalId).map {
                journeyConfig =>
                  journeyConfig.businessEntity match {
                    case LimitedCompany =>
                      BadRequest(ctutr_view(journeyConfig.pageConfig, routes.CaptureCtutrController.submit(journeyId), formWithErrors))
                    case RegisteredSociety =>
                      BadRequest(optional_ctutr_view(journeyId, journeyConfig.pageConfig, routes.CaptureCtutrController.submit(journeyId), formWithErrors))
                    case invalidEntity =>
                      throw new InternalServerException(s"Invalid entity: $invalidEntity on CTUTR page")
                  }
              }
            },
            ctutr =>
              storageService.storeCtutr(journeyId, ctutr).map {
                _ => Redirect(routes.CheckYourAnswersController.show(journeyId))
              }
          )
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def noCtutr(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
            _ =>
              storageService.removeCtutr(journeyId).map {
                _ => Redirect(routes.CheckYourAnswersController.show(journeyId))
              }
          }
        case _ =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

}
