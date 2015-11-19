package crawler.news.model

/**
 * 新闻页详情
 * Created by Yang Jing (yangbajing@gmail.com) on 2015-11-05.
 * @param url 网页链接
 * @param src 网页源码
// * @param title 新闻标题
// * @param time 发布时间
 * @param content 新闻内容
 */
case class NewsPageItem(url: String,
                        src: String,
//                        title: String,
//                        time: String,
                        content: String)
