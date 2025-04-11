import org.slf4j.{Logger, LoggerFactory}

import java.io.File
import java.nio.file.{Files, Path}
import java.security.{DigestInputStream, MessageDigest}

package object processing {

  val logger: Logger = LoggerFactory.getLogger("processing")

  object implicits {
    implicit class RichFile(f: File) {
      def ensureDirsExist(): File = {
        f.mkdirs()
        f
      }

      def checkMd5(requiredMd5: String): Boolean = {
        MessageDigest.getInstance("MD5").md5Matches(f, requiredMd5)
      }

      def checkMinSize: Boolean = {
        val actualSize = f.length()
        val minSize = minSizeFor(f.getName)
        logger.info(s">>> actualSize: ${actualSize}, minSize: ${minSize}")
        actualSize > minSize
      }

      private val filePattern = "AB..(GB|IS)_CSV.zip".r
      private def minSizeFor(fileName: String): Long = fileName match {
        case filePattern("GB") => 9500000000L
        case filePattern("IS") => 150000000L
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
      private def md5ForFile(f: File): String = {
        val dis = new DigestInputStream(Files.newInputStream(f.toPath), md)
        val buffer = new Array[Byte](8192)

        while (dis.read(buffer) != -1) {}

        md.digest.map("%02x".format(_)).mkString
      }

      def md5Matches(f: File, md5: String): Boolean = {
        logger.info(s"Calculating MD5 for file: ${f.getAbsolutePath}")
        val actualMd5 = md5ForFile(f)
        logger.info(s">>> actualMd5: $actualMd5, requiredMd5: $md5")
        actualMd5 == md5
      }
    }

    implicit class RichString(s: String) {
      def dbSafe: String =
        s.replaceAll("AddressBase Premium ", "ABP")
          .replaceAll("\\.", "")

    }
  }
}
