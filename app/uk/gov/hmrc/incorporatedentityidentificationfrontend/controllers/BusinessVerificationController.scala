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

import scala.concurrent.ExecutionContext

@Singleton
class BusinessVerificationController @Inject()(mcc: MessagesControllerComponents,
                                               val authConnector: AuthConnector,
                                               businessVerificationService: BusinessVerificationService,
                                               incorporatedEntityInformationService: IncorporatedEntityInformationService
                                              )(implicit val executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def startBusinessVerificationJourney(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised() {
        val optCtutr = incorporatedEntityInformationService.retrieveCtutr(journeyId)
        optCtutr.flatMap {
          case Some(ctutr) =>
            businessVerificationService.createBusinessVerificationJourney(journeyId, ctutr).map {
              case Some(redirectUri) =>
                Redirect(redirectUri)
              case None => NotImplemented
            }
          case None =>
            throw new InternalServerException(s"There is no CTUTR for $journeyId")
        }
      }
  }

  def retrieveBusinessVerificationResult(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised() {
        val optBusinessVerificationJourneyId = req.getQueryString("journeyId")

        optBusinessVerificationJourneyId match {
          case Some(businessVerificationJourneyId) =>
            businessVerificationService.retrieveBusinessVerificationStatus(businessVerificationJourneyId).flatMap {
              verificationStatus =>
                incorporatedEntityInformationService.storeBusinessVerificationStatus(journeyId, verificationStatus).map {
                  _ => Redirect(routes.RegistrationController.register(journeyId))
                }
            }
          case None =>
            throw new InternalServerException("Missing JourneyID from Business Verification callback")
        }
      }
  }

}