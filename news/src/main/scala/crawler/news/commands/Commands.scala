package crawler.news.commands

import crawler.news.enums.{SearchMethod, NewsSource}
import crawler.news.model.{NewsPageItem, NewsItem, NewsResult}

import scala.concurrent.duration.FiniteDuration

case class RequestSearchNews(sources: Seq[NewsSource.Value], msg: SearchNews)

/**
 * 新闻源搜索
 * @param key 关键词
 * @param method 搜索方式
 * @param duration 持续时间（超时）
 */
case class SearchNews(key: String,
                      method: SearchMethod.Value,
                      duration: FiniteDuration)

/**
 * 开始搜索新闻
 */
case object StartSearchNews

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
case class SearchPageResult(news: NewsResult)

/**
 * 搜索失败
 * @param failure 失败结果
 */
case class SearchPageFailure(failure: Throwable)

/**
 * 开始抓取新闻详情内容
 */
case object StartFetchItemPage

/**
 * 新闻详情
 * @param result 新闻详情
 */
case class ItemPageResult(result: Either[String, NewsPageItem])
