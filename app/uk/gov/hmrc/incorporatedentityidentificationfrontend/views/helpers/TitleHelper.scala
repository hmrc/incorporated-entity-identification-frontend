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

package uk.gov.hmrc.incorporatedentityidentificationfrontend.views.helpers

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.PageConfig

object TitleHelper {
  def title(titleKey: String, pageConfig: PageConfig, form: Form[_])(implicit messages: Messages, appConfig: AppConfig): String =
    title(titleKey, pageConfig, form.hasErrors)

  def title(titleKey: String, pageConfig: PageConfig, isAnErrorPage: Boolean = false)(implicit messages: Messages, appConfig: AppConfig): String = {
    val titleMessage: String = s"${messages(titleKey)} - ${pageConfig.optServiceName.getOrElse(messages("service.name.default"))} - ${messages("service.govuk")}"

    if (isAnErrorPage) messages("error.title-prefix") + titleMessage
    else titleMessage
  }
}
