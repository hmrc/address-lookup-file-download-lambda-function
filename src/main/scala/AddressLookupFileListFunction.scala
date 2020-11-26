import java.io.File
import java.net.URL
import java.util.{Map => jMap}

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import scala.collection.JavaConverters.{mapAsJavaMapConverter, seqAsJavaListConverter}

class AddressLookupFileListFunction extends RequestHandler[String, jMap[String, Object]] {
  override def handleRequest(input: String, context: Context): jMap[String, Object] = {
    // We should only ever get one epoch back from listAllFileUrlsToDownload
    val epochResults = AddressLookup.listAllFileUrlsToDownload().groupBy(_.epoch)
    val (epoch, products) = epochResults.head

    val results = Map(
        "epoch" -> epoch.toString,
        "batches" -> products.flatMap { p =>
          p.zips
            .map(_.url)
            .grouped(25)
            .zipWithIndex
            .map { case (batch, idx) =>
              val batchDir = AddressLookup.batchTargetDirectory(p.productName, p.epoch, idx)
              Map(
                "batchDir" -> batchDir,
                "files" -> batch.filterNot(z =>
                  new File(s"$batchDir/${fileOf(z)}.done").exists()
                ).asJava
              ).asJava
            }
        }.asJava)

    println(results.mkString)
    results.asJava
  }

  private def fileOf(url: URL): String = {
    val file = url.getPath
    val slash = file.lastIndexOf('/')
    file.substring(slash + 1)
  }
}


object Main extends App {
  new AddressLookupFileListFunction().handleRequest("", null)
}

