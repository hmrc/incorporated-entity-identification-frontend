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

package views.helpers

import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.models.BusinessEntity._
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.helpers.CheckYourAnswersRowBuilder

class CheckYourAnswersRowBuilderSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val rowBuilder: CheckYourAnswersRowBuilder = new CheckYourAnswersRowBuilder()
  val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val testCompanyNumberRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("Company registration number")),
    value = Value(HtmlContent(testCompanyNumber)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureCompanyNumberController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Company registration number")
      )
    )))
  )

  val testCtutrRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("Unique Taxpayer Reference (UTR)")),
    value = Value(HtmlContent(testCtutr)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureCtutrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique Taxpayer Reference (UTR)")
      )
    )))
  )

  val testNoCtutrRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("Unique Taxpayer Reference (UTR)")),
    value = Value(HtmlContent("The business does not have a UTR")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureCtutrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique Taxpayer Reference (UTR)")
      )
    )))
  )

  val testChrnRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("HMRC reference number")),
    value = Value(HtmlContent(testCHRN)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureCHRNController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("HMRC reference number")
      )
    )))
  )

  val testNoChrnRow: SummaryListRow = SummaryListRow(
    key = Key(content = Text("HMRC reference number")),
    value = Value(HtmlContent("The charity does not have a HMRC reference number")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureCHRNController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("HMRC reference number")
      )
    )))
  )

  "buildSummaryListRows" should {
    "build a summary list sequence" when {
      "the user enters a CRN and CTUTR" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilder.buildSummaryListRows(
          journeyId = testJourneyId,
          optCompanyProfile = Some(testCompanyProfile),
          optCtutr = Some(testCtutr),
          optChrn = None,
          journeyConfig = testJourneyConfig(LimitedCompany)
        )(messages)

        actualSummaryList mustBe Seq(
          testCompanyNumberRow,
          testCtutrRow
        )
      }

      "the user enters a CRN and no CTUTR" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilder.buildSummaryListRows(
          journeyId = testJourneyId,
          optCompanyProfile = Some(testCompanyProfile),
          optCtutr = None,
          optChrn = None,
          journeyConfig = testJourneyConfig(LimitedCompany)
        )(messages)

        actualSummaryList mustBe Seq(
          testCompanyNumberRow,
          testNoCtutrRow
        )
      }

      "the user enters a CRN and CHRN" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilder.buildSummaryListRows(
          journeyId = testJourneyId,
          optCompanyProfile = Some(testCompanyProfile),
          optCtutr = None,
          optChrn = Some(testCHRN),
          journeyConfig = testJourneyConfig(CharitableIncorporatedOrganisation)
        )(messages)

        actualSummaryList mustBe Seq(
          testCompanyNumberRow,
          testChrnRow
        )
      }

      "the user enters a CRN and No CHRN" in {

        val actualSummaryList: Seq[SummaryListRow] = rowBuilder.buildSummaryListRows(
          journeyId = testJourneyId,
          optCompanyProfile = Some(testCompanyProfile),
          optCtutr = None,
          optChrn = None,
          journeyConfig = testJourneyConfig(CharitableIncorporatedOrganisation)
        )(messages)

        actualSummaryList mustBe Seq(
          testCompanyNumberRow,
          testNoChrnRow
        )
      }


    }
  }
}
