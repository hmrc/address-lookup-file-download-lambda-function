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

import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.github.sardine.{DavResource, Sardine}

class WebdavFetcher(factory: SardineWrapper, val downloadFolder: File, status: LambdaLogger) {

  // Downloads a specified set of remote files, marks them all with a completion marker (.done),
  // then returns the total bytes copied.
  def fetchList(product: OSGBProduct, outputPath: String, forceFetch: Boolean): List[DownloadItem] = {
    status.log(s"Product bundle: $product")
    val outputDirectory = resolveAndMkdirs(outputPath)
    val sardine = factory.begin
    for (webDavFile <- product.zips) yield {
      fetchFile(webDavFile.url, sardine, outputDirectory, forceFetch)
    }
  }

  private def toUrl(base: String, res: DavResource): URL = {
    val myUrl = new URL(base)
    new URL(myUrl.getProtocol, myUrl.getHost, myUrl.getPort, res.getHref.getPath)
  }

  def fetchFile(url: URL, sardine: Sardine, outputDirectory: File, forceFetch: Boolean = true): DownloadItem = {
    val file = fileOf(url)
    val outFile = DownloadedFile(outputDirectory, file)
    if (forceFetch || !outFile.exists || outFile.isIncomplete) {
      // pre-existing files are considered incomplete
      outFile.delete()

      val ff = if (forceFetch) " (forced)" else ""
      status.log(s"Fetching $url to $file$ff.")
      val fetched = doFetchFile(url, sardine, outFile)
      outFile.touchDoneFile()
      status.log(s"Fetched $file.")
      DownloadItem.fresh(fetched)

    } else {
      status.log(s"Already had $file.")
      DownloadItem.stale(outFile)
    }
  }

  private def doFetchFile(url: URL, sardine: Sardine, outFile: DownloadedFile): DownloadedFile = {
    println(s"About to connect to HFS ...")
    val in = sardine.get(url.toExternalForm)
    println(s"Connected to HFS")

    try {
      println(s"About to copy from HFS ...")
      Files.copy(in, outFile.toPath)
      println(s"Copied from HFS")
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

  private def resolveAndMkdirs(outputPath: String): File = {
    val outputDirectory = if (outputPath.nonEmpty) new File(downloadFolder, outputPath) else downloadFolder
    outputDirectory.mkdirs()
    outputDirectory
  }
}
