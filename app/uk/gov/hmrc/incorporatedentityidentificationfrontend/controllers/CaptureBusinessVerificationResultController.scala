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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.{BusinessVerificationService, IncorporatedEntityInformationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureBusinessVerificationResultController @Inject()(mcc: MessagesControllerComponents,
                                                            val authConnector: AuthConnector,
                                                            businessVerificationService: BusinessVerificationService,
                                                            incorporatedEntityInformationService: IncorporatedEntityInformationService
                                                           )(implicit val executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(Ok)
      }
  }

  def startBusinessVerificationJourney(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      val ctutr = incorporatedEntityInformationService.retrieveCtutr(journeyId)
      ctutr.flatMap {
        case Some(ctutr) =>
          businessVerificationService.createBusinessVerificationJourney(ctutr).map {
            case Some(redirectUri) => Redirect(redirectUri)
            case None => NotImplemented
          }
        case None =>
          throw new InternalServerException(s"There is no CTUTR for $journeyId")
      }
  }
}