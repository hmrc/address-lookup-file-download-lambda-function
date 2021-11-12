import java.util.{Map => JMap}
import scala.collection.JavaConverters._

case class AddressLookupFileListRequest(epoch: Option[String] = None, forceDownload: Boolean)

object AddressLookupFileListRequest {
  def apply(requestJMap: JMap[String, Object]): AddressLookupFileListRequest = {
    AddressLookupFileListRequest(
      epoch = Option(requestJMap.get("epoch").asInstanceOf[String]),
      forceDownload = Option(requestJMap.get("forceDownload").asInstanceOf[Boolean]).getOrElse(false))
  }
}