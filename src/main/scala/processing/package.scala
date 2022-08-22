import java.io.File
import java.nio.file.{Files, Path}
import java.security.{DigestInputStream, MessageDigest}

package object processing {
  object implicits {
    implicit class RichFile(f: File) {
      def ensureDirsExist(): File = {
        f.mkdirs()
        f
      }
    }

    implicit class RichPath(p: Path) {
      def ensureDirsExist(): Path = {
        val pp = if (p.toFile.isFile) p.getParent else p
        pp.toFile.mkdirs()
        p
      }
    }

    implicit class RichMessageDigest(md: MessageDigest) {
      def md5ForFile(f: File): String = {
        val buffer = new Array[Byte](4096)
        val dis = new DigestInputStream(Files.newInputStream(f.toPath), md)
        while (dis.available > 0) {
          dis.read(buffer)
        }
        md.digest.map(b => String.format("%02x", Byte.box(b))).mkString
      }

      def md5Matches(f: File, md5: String): Boolean = {
        md5ForFile(f) == md5
      }
    }

    implicit class RichString(s: String) {
      def dbSafe: String =
        s.replaceAll("AddressBase Premium ", "ABP")
          .replaceAll("\\.", "")

    }
  }
}
