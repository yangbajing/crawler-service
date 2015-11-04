package crawler.news.commands

import crawler.news.NewsSource

case class SearchNews(name: String, source: NewsSource.Value)

case object StartFetchSearchPage
