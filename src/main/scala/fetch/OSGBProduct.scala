package fetch

import java.net.URL

case class OSGBProduct(productName: String, epoch: Int, zips: List[URL]) extends Ordered[OSGBProduct] {
  override def compare(that: OSGBProduct): Int = this.epoch - that.epoch
}