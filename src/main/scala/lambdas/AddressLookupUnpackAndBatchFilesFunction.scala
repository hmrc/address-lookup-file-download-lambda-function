package lambdas

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import processing.{FileDownloader, FileProcessor}

import java.util.{List => jList, Map => jMap}
import scala.collection.JavaConverters._

class AddressLookupUnpackAndBatchFilesFunction extends RequestHandler[java.util.Map[String, Object], jMap[String, Object]] {
  override def handleRequest(batchInfo: java.util.Map[String, Object], context: Context): jMap[String, Object] = {
    val batchesRootDir = batchInfo.get("batchesRootDir").asInstanceOf[String]
    val downloadedFiles = batchInfo.get("downloadedFiles").asInstanceOf[jList[String]].asScala.toList

    doUnpackAndBatchFiles(batchesRootDir, downloadedFiles)
  }

  def doUnpackAndBatchFiles(batchesRootDir: String, downloadedFiles: List[String]): jMap[String, Object] = {
    val processor = FileProcessor()
    val result = processor.process(downloadedFiles, batchesRootDir)
    val jResult = result.map {
      case (bd, bs) => Map("batchDir" -> bd.getAbsolutePath, "batchFiles" -> bs.map(p =>
        p.toFile.getAbsolutePath).toList.asJava).asJava
    }.toList.asJava
    val epoch = result.head._1.getParentFile.getName

    Map("batches" -> jResult, "epoch" -> epoch).asJava
  }
}
