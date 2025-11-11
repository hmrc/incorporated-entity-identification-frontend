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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.config

import play.api.i18n.MessagesApi
import play.api.mvc.Results.{BadRequest, NotFound}
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.{Configuration, Environment, Logging}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.templates.error_template
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject()(view: error_template,
                             val messagesApi: MessagesApi,
                             val config: Configuration,
                             val env: Environment
                           )(implicit appConfig: AppConfig) extends FrontendErrorHandler with AuthRedirects with Logging {
  
  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    if (play.mvc.Http.Status.BAD_REQUEST == statusCode)
      Future.successful(BadRequest(message))
    else
      super.onClientError(request, statusCode, message)

  override def standardErrorTemplate(pageTitle: String,
                                     heading: String,
                                     message: String)(implicit request: RequestHeader): Future[Html] =
    Future.successful(view(pageTitle, heading, message))

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case _: AuthorisationException => resolveError(request, exception)
      case _ => super.onServerError(request, exception)
    }
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Future[Result] = {
    ex match {
      case _: AuthorisationException =>
        logger.debug("[AuthenticationPredicate][async] Unauthorised request. Redirect to Sign In.")
        Future.successful(toGGLogin(rh.path))
      case _: NotFoundException =>
        notFoundTemplate(Request(rh, "")).map(html => NotFound(html))
      case _ =>
        super.resolveError(rh, ex)
    }
  }
}
