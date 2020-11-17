import java.io.File
import java.net.URL

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import scala.collection.JavaConverters.{mapAsJavaMapConverter, seqAsJavaListConverter}

class AddressLookupFileListFunction extends RequestHandler[String, java.util.List[java.util.Map[String, Any]]] {
  override def handleRequest(input: String, context: Context): java.util.List[java.util.Map[String, Any]] =
    {
      val results = AddressLookup.listAllFileUrlsToDownload()
        .flatMap { p =>
          p.zips
            .filterNot(z => new File(s"/mnt/efs/${p.productName}/${p.epoch}/${fileOf(z.url)}").exists())
            .map(f => Map(
              "productName" -> p.productName,
              "epoch" -> p.epoch,
              "fileUrl" -> f.url).asJava)
        }

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

