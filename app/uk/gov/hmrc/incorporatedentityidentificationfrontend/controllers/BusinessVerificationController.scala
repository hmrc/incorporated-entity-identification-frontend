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
import uk.gov.hmrc.incorporatedentityidentificationfrontend.services.{BusinessVerificationService, JourneyService, StorageService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessVerificationController @Inject()(mcc: MessagesControllerComponents,
                                               val authConnector: AuthConnector,
                                               businessVerificationService: BusinessVerificationService,
                                               journeyService: JourneyService,
                                               storageService: StorageService
                                              )(implicit val executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def startBusinessVerificationJourney(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised().retrieve(internalId) {
        case Some(authInternalId) =>
          journeyService.getJourneyConfig(journeyId, authInternalId).flatMap {
            journeyConfig =>
              storageService.retrieveCtutr(journeyId).flatMap {
                case Some(ctutr) =>
                  businessVerificationService
                    .createBusinessVerificationJourney(journeyId, ctutr, journeyConfig).flatMap {
                    case Some(redirectUri) =>
                      Future.successful(Redirect(redirectUri))
                    case None =>
                      Future.successful(Redirect(routes.RegistrationController.register(journeyId)))
                  }
                case None =>
                  throw new InternalServerException(s"No CTUTR found in the database for $journeyId")
              }
          }
        case None => throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def retrieveBusinessVerificationResult(journeyId: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised() {
        req.getQueryString("journeyId") match {
          case Some(businessVerificationJourneyId) =>
            businessVerificationService.retrieveBusinessVerificationStatus(businessVerificationJourneyId).flatMap {
              verificationStatus =>
                storageService.storeBusinessVerificationStatus(journeyId, verificationStatus).map {
                  _ => Redirect(routes.RegistrationController.register(journeyId))
                }
            }
          case None =>
            throw new InternalServerException("JourneyID is missing from Business Verification callback")
        }
      }
  }

}