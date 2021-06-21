package fetch

case class OSGBProduct(productName: String, epoch: Int, zips: List[WebDavFile]) extends Ordered[OSGBProduct] {
  override def compare(that: OSGBProduct): Int = this.epoch - that.epoch
}
