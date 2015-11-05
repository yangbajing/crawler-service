package crawler.news.commands

import akka.actor.ActorRef
import crawler.news.model.{NewsItem, NewsResult}
import crawler.news.{NewsSource, SearchMethod}

import scala.concurrent.duration.FiniteDuration

/**
 * 新闻搜索请求
 * @param key 关键词
 * @param sources 搜索源
 * @param method 搜索方式
 * @param duration 持续时间（超时）
 */
case class RequestSearchNews(key: String,
                             sources: Seq[NewsSource.Value],
                             method: SearchMethod.Value,
                             duration: FiniteDuration)

/**
 * 开始搜索新闻
 * @param key 搜索关键词
 * @param source 搜索源
 */
case class SearchNews(key: String, source: NewsSource.Value, doSender: ActorRef)

/**
 * 抓取搜索页
 */
case object StartFetchSearchPage

/**
 * 搜索超时
 */
case object SearchTimeout

/**
 * 搜索结果
 * @param news 新闻结果
 */
case class SearchResult(news: NewsResult)

/**
 * 搜索失败
 * @param e 失败结果
 */
case class SearchFailure(e: RuntimeException)

/**
 * 开始抓取新闻详情内容
 */
case object StartFetchItemPage

/**
 * 新闻详情
 * @param item 新闻详情
 */
case class ItemPageResult(item: NewsItem)
