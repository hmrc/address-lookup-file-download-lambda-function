import fetch.OSGBProduct

import java.net.URL
import java.util.{Map => JMap, List => JList}

case class AddressLookupFileListResponse(epoch: String, batches: Seq[Batch])

case class Batch(batchDir: String, files: List[URL])

object AddressLookupFileListResponse {
  def apply(epoch: String, osgbProducts: Seq[OSGBProduct], batchTargetDirectoryFn: (String, Int, Int) => String, filesAlreadyDownloaded: Seq[String] = Seq()): AddressLookupFileListResponse = {
    val products = osgbProducts.flatMap { p =>
      p.zips
       .map(_.url)
       .grouped(25)
       .zipWithIndex
       .map { case (files, idx) =>
         val batchDir = batchTargetDirectoryFn(p.productName, p.epoch, idx)
         Batch(batchDir, files.filterNot{z =>
             filesAlreadyDownloaded.contains(s"$batchDir/${fileOf(z)}.done")
         })
       }
    }

    AddressLookupFileListResponse(epoch, products)
  }

  private def fileOf(url: URL): String = {
    val file = url.getPath
    val slash = file.lastIndexOf('/')
    file.substring(slash + 1)
  }

  implicit class AddressLookupFileListResponseWithJavaSupport(lookupFileListResponse: AddressLookupFileListResponse) {
    def asJava(): JMap[String, Object] = {
      import scala.collection.JavaConverters._
      import Batch._
      Map(
        "epoch" -> lookupFileListResponse.epoch,
        "batches" -> lookupFileListResponse.batches.asJava
      ).asJava
    }
  }
}

object Batch {
  implicit class BatchWithJavaSupport(batches: Seq[Batch]) {
    def asJava(): JList[JMap[String, Object]] = {
      import scala.collection.JavaConverters._

      batches.map(b => Map("batchDir" -> b.batchDir, "files" -> b.files.asJava).asJava).asJava
    }
  }
}