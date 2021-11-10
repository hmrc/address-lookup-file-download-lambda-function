import java.util.{Map => JMap}
import scala.collection.JavaConverters._

case class AddressLookupFileListRequest(epoch: Option[String] = None, redownloadFiles: Boolean)

object AddressLookupFileListRequest {
  def apply(requestJMap: JMap[String, Object]): AddressLookupFileListRequest = {
    val requestMap = requestJMap.asScala
    AddressLookupFileListRequest(
      epoch = Option(requestJMap.get("epoch").asInstanceOf[String]),
      redownloadFiles = Option(requestJMap.get("redownloadFiles").asInstanceOf[Boolean]).getOrElse(false))
  }
}