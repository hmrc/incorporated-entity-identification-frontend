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

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.{CompanyProfile, JourneyConfig}
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity.{CharitableIncorporatedOrganisation, LimitedCompany, RegisteredSociety}

import javax.inject.{Inject, Singleton}

@Singleton
class CheckYourAnswersRowBuilder @Inject()() {

  def buildSummaryListRows(journeyId: String,
                           optCompanyProfile: Option[CompanyProfile],
                           optCtutr: Option[String],
                           optChrn: Option[String],
                           journeyConfig: JourneyConfig
                          )(implicit messages: Messages): Seq[SummaryListRow] = {

    val companyNumberRow = buildSummaryRow(
      messages("check-your-answers.company_number"),
      optCompanyProfile match {
        case Some(companyProfile) => companyProfile.companyNumber
      },
      routes.CaptureCompanyNumberController.show(journeyId)
    )

    def ctutrRow (): Aliases.SummaryListRow =
      buildSummaryRow(
        messages("check-your-answers.ctutr"),
        optCtutr match {
          case Some(ctutr) => ctutr
          case None => messages("check-your-answers.no-ctutr")
        },
        routes.CaptureCtutrController.show(journeyId)
      )

    def chrnRow (): Aliases.SummaryListRow=
      buildSummaryRow(
        messages("check-your-answers.chrn"),
        optChrn match {
          case Some(chrn) => chrn
          case None => messages("check-your-answers.no-chrn")
        },
        routes.CaptureCHRNController.show(journeyId)
      )

    journeyConfig.businessEntity match {
      case LimitedCompany => Seq(companyNumberRow, ctutrRow())
      case RegisteredSociety => Seq(companyNumberRow, ctutrRow())
      case CharitableIncorporatedOrganisation => Seq(companyNumberRow, chrnRow())
      case _ => throw new IllegalStateException("Data could not be retrieved from database or does not exist in database")
    }

  }

  private def buildSummaryRow(key: String, value: String, changeLink: Call)(implicit messages: Messages) = SummaryListRow(
    key = Key(content = Text(key)),
    value = Value(HtmlContent(value)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = changeLink.url,
        content = Text(messages("base.change")),
        visuallyHiddenText = Some(key)
      )
    )))
  )
}


