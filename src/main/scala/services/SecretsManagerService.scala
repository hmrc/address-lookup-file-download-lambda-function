package services

import com.amazonaws.secretsmanager.caching.SecretCache
import play.api.libs.json.Json

class SecretsManagerService(secretCache: SecretCache) {

  def getSecret(secretName: String, secretKey: String): String = {
    val secretString = secretCache.getSecretString(secretName)
    val secretAsJSON = Json.parse(secretString)

    secretAsJSON(secretKey).as[String]
  }
}