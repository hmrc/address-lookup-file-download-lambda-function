package processing

import me.lamouri.JCredStash

import scala.collection.JavaConverters._

class Credentials() {
  private val credstash = new JCredStash()
  private val context = Map("role" -> "address_lookup_file_download").asJava
  private val credStashTable = "credential-store"

  def secret(name: String): String = credstash.getSecret(credStashTable, name, context)
}
