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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.utils

import play.api.http.HttpConfiguration
import play.api.i18n.{DefaultMessagesApi, DefaultMessagesApiProvider, Langs}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.JourneyConfig

import javax.inject.{Inject, Singleton}

object MessagesHelper {

  private val optServiceNameTranslationKey = "optServiceName"

  def amendMessagesWithLabelsFromJourneyConfig(initialMessages: Map[String, Map[String, String]],
                                               journeyConfig: JourneyConfig): Map[String, Map[String, String]] = {
    val extraEnglishTranslations: Map[String, String] = journeyConfig
      .pageConfig
      .optServiceName
      .map(serviceName => Map(optServiceNameTranslationKey -> serviceName))
      .getOrElse(Map.empty)

    val extraWelshTranslations: Map[String, String] = journeyConfig
      .pageConfig
      .optLabels
      .map(labels => Map(optServiceNameTranslationKey -> labels.optWelshServiceName))
      .getOrElse(Map.empty)

    initialMessages.map {
      case (lang@"en", oldMessages) => lang -> (oldMessages ++ extraEnglishTranslations)
      case (lang@"cy", oldMessages) => lang -> (oldMessages ++ extraWelshTranslations)
      case (lang, oldMessages) => lang -> oldMessages
    }
  }
}

@Singleton
class MessagesHelper @Inject()(environment: Environment,
                               config: Configuration,
                               langs: Langs,
                               httpConfiguration: HttpConfiguration
                              ) extends DefaultMessagesApiProvider(environment, config, langs, httpConfiguration) {

  lazy val defaultMessages: Map[String, Map[String, String]] = loadAllMessages

  def getRemoteMessagesApi(journeyConfig: JourneyConfig): DefaultMessagesApi =
    new DefaultMessagesApi(
      MessagesHelper.amendMessagesWithLabelsFromJourneyConfig(defaultMessages, journeyConfig),
      langs,
      langCookieName = langCookieName,
      langCookieSecure = langCookieSecure,
      langCookieHttpOnly = langCookieHttpOnly,
      httpConfiguration = httpConfiguration
    )

}