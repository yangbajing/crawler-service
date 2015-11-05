package crawler.news.commands

import akka.actor.ActorRef
import crawler.news.SearchMethod
import crawler.news.model.{NewsItem, NewsResult}

import scala.concurrent.duration.FiniteDuration

/**
 * 新闻搜索请求
 * @param key 关键词
 * @param method 搜索方式
 * @param duration 持续时间（超时）
 */
case class RequestSearchNews(key: String,
                             method: SearchMethod.Value,
                             duration: FiniteDuration)

/**
 * 开始搜索新闻
 * @param key 搜索关键词
 * @param reqSender 发起请求的ActorRef
 */
case class SearchNews(key: String, reqSender: ActorRef)

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
 * @param key 搜索关键词
 * @param failure 失败结果
 */
case class SearchFailure(key: String, failure: Throwable)

/**
 * 开始抓取新闻详情内容
 */
case object StartFetchItemPage

/**
 * 新闻详情
 * @param item 新闻详情
 */
case class ItemPageResult(item: NewsItem)
