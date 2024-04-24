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

package test.uk.gov.hmrc.incorporatedentityidentificationfrontend.views

import play.api.i18n.{Lang, MessagesApi}
import test.uk.gov.hmrc.incorporatedentityidentificationfrontend.utils.ComponentSpecHelper

import scala.io.{BufferedSource, Source}

class WelshLanguageISpec extends ComponentSpecHelper {

  val englishMessages: BufferedSource = Source.fromResource("messages")
  val welshMessages: BufferedSource = Source.fromResource("messages.cy")

  val messageKeysEnglish: List[String] = getMessageKeys(englishMessages).toList
  val messageKeysWelsh: List[String] = getMessageKeys(welshMessages).toList
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "English messages must have the same keys as Welsh messages" in {
    for (key <- messageKeysEnglish) {
      messageKeysWelsh.contains(key) mustBe true
    }
  }

  "Welsh messages must have the same keys as English messages" in {
    for (key <- messageKeysWelsh) {
      messageKeysEnglish.contains(key) mustBe true
    }
  }

  "An example of the welsh text can be retrieved" in {
    messagesApi("service.name.default")(Lang("cy")) mustBe "Gwasanaeth Dilysu Endid"
  }

  private def getMessageKeys(source: Source) = {
    source
      .getLines
      .map(_.trim)
      .filter(!_.startsWith("#"))
      .filter(_.nonEmpty)
      .map(_.split(' ').head)
  }

  englishMessages.close()
  welshMessages.close()

}
