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

package fetch

import java.io.File
import java.net.URL
import java.nio.file._

import com.github.sardine.Sardine

class WebdavFetcher(factory: SardineWrapper, val downloadFolder: File) {

  def fetchFile(url: URL, outputDirectory: File, forceFetch: Boolean = true): DownloadItem = {
    val file = fileOf(url)
    val outFile = DownloadedFile(outputDirectory, file)
    if (forceFetch || !outFile.exists || outFile.isIncomplete) {
      // pre-existing files are considered incomplete
      outFile.delete()

      val ff = if (forceFetch) " (forced)" else ""
      println(s"Fetching $url to $file$ff.")
      val fetched = doFetchFile(url, factory.begin, outFile)
      outFile.touchDoneFile()
      println(s"Fetched $file.")
      DownloadItem.fresh(fetched)

    } else {
      println(s"Already had $file.")
      DownloadItem.stale(outFile)
    }
  }

  private def doFetchFile(url: URL, sardine: Sardine, outFile: DownloadedFile): DownloadedFile = {
    val in = sardine.get(url.toExternalForm)

    try {
      Files.copy(in, outFile.toPath)
      outFile
    } finally {
      in.close()
    }
  }

  private def fileOf(url: URL): String = {
    val file = url.getPath
    val slash = file.lastIndexOf('/')
    file.substring(slash + 1)
  }
}
