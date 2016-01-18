package crawler.app.news

import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

/**
 * News Utils
 * Created by yangjing on 15-11-5.
 */
object NewsUtils {
  private val _nums = new AtomicInteger(0)

  def getIndent = _nums.getAndIncrement()

  def uriToBaseUri(uri: String): String = uriToBaseUri(URI.create(uri))

  def uriToBaseUri(uri: URI): String = {
    val sb = new StringBuffer()
    if (uri.getScheme != null) {
      sb.append(uri.getScheme)
      sb.append(':')
    }
    if (uri.isOpaque) {
      sb.append(uri.getSchemeSpecificPart)
    } else {
      if (uri.getHost != null) {
        sb.append("//")
        if (uri.getUserInfo != null) {
          sb.append(uri.getUserInfo)
          sb.append('@')
        }
        val needBrackets = ((uri.getHost.indexOf(':') >= 0)
          && !uri.getHost.startsWith("[")
          && !uri.getHost.endsWith("]"))
        if (needBrackets) {
          sb.append('[')
        }
        sb.append(uri.getHost)
        if (needBrackets) sb.append(']')
        if (uri.getPort != -1) {
          sb.append(':')
          sb.append(uri.getPort)
        }
      } else if (uri.getAuthority != null) {
        sb.append("//")
        sb.append(uri.getAuthority)
      }
    }
    sb.toString
  }
}
