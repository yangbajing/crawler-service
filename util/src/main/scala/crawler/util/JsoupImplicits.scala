package crawler.util

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * Jsoup 相关辅助方法
 * Created by yangjing on 15-11-3.
 */
object JsoupImplicits {

  implicit class JsoupElementFindByClassname(element: Element) {
    def findByClass(cn: String): Elements = {
      element.getElementsByClass(cn)
    }
  }

  implicit class JsoupElementsFindByClassname(elements: Elements) {
    def findByClass(cn: String): Elements = {
      val list = new java.util.LinkedList[Element]()
      val iter = elements.iterator()
      while (iter.hasNext) {
        val elements = iter.next().getElementsByClass(cn)
        list.addAll(elements)
      }
      new Elements(list)
    }
  }

}
