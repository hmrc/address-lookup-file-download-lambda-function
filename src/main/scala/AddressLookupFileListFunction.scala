import java.io.File
import java.net.URL
import java.util.{List => jList, Map => jMap}

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import fetch.OSGBProduct

import scala.collection.JavaConverters.{mapAsJavaMapConverter, seqAsJavaListConverter}

class AddressLookupFileListFunction extends RequestHandler[String, jMap[Int, jList[jMap[String, Object]]]] {
  override def handleRequest(input: String, context: Context): jMap[Int, jList[jMap[String, Object]]] = {

    val epochResults = AddressLookup.listAllFileUrlsToDownload()
      .foldLeft(Map[Int, Seq[OSGBProduct]]()) { case (map, product) =>
        map ++ Map(product.epoch -> (map.getOrElse(product.epoch, Seq()) :+ product))
      }

    val results = epochResults.map { case (epoch, products) =>
      epoch -> products.flatMap { p =>
        p.zips
          .map(_.url)
          .grouped(25)
          .zipWithIndex
          .map { case (batch, idx) =>
            Map(
              "product" -> p.productName,
              "batchIndex" -> idx.toString,
              "files" -> batch.filterNot(z =>
                new File(s"/mnt/efs/${p.productName}/${p.epoch}/$idx/${fileOf(z)}.done").exists()
              ).asJava
            ).asJava
          }
      }.asJava
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

