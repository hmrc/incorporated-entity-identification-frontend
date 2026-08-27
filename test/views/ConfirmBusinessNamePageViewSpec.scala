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

package views

import _root_.helpers.TestConstants.{testCompanyName, testJourneyId, testLimitedCompanyJourneyConfig}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.incorporatedentityidentificationfrontend.config.AppConfig
import uk.gov.hmrc.incorporatedentityidentificationfrontend.controllers.routes
import uk.gov.hmrc.incorporatedentityidentificationfrontend.forms.ConfirmBusinessNameForm
import uk.gov.hmrc.incorporatedentityidentificationfrontend.views.html.confirm_business_name_page

class ConfirmBusinessNamePageViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  private val page = app.injector.instanceOf[confirm_business_name_page]
  private implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
  private implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private def view(formValue: String): Document =
    Jsoup.parse(
      page(
        pageConfig = testLimitedCompanyJourneyConfig.pageConfig,
        form = ConfirmBusinessNameForm.form(messages).fill(formValue),
        formAction = routes.ConfirmBusinessNameController.submit(testJourneyId),
        companyName = testCompanyName,
        journeyId = testJourneyId
      )(FakeRequest(), messages, appConfig).body
    )

  "ConfirmBusinessNamePageView" should {
    "preselect yes when the stored answer is yes" in {
      val document = view(ConfirmBusinessNameForm.yes)

      document.select("input[value=yes]").first().hasAttr("checked") mustBe true
      document.select("input[value=no]").first().hasAttr("checked") mustBe false
    }

    "preselect no when the stored answer is no" in {
      val document = view(ConfirmBusinessNameForm.no)

      document.select("input[value=yes]").first().hasAttr("checked") mustBe false
      document.select("input[value=no]").first().hasAttr("checked") mustBe true
    }
  }
}
