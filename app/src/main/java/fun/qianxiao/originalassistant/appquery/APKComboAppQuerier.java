package fun.qianxiao.originalassistant.appquery;

import com.blankj.utilcode.util.GsonUtils;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import fun.qianxiao.originalassistant.api.appquery.APKComboQueryApi;
import fun.qianxiao.originalassistant.bean.AnalysisResult;
import fun.qianxiao.originalassistant.manager.AppQueryManager;
import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.List;

public class APKComboAppQuerier extends AbstractAppQuerier<APKComboAppQueryApi, JSONObject> {
    @Override
    protected AppQueryManager.AppQueryChannel getFromChannel() {
        return AppQueryManager.AppQueryChannel.APKCOMBO;
    }

    @Override
    protected Observable<ResponseBody> search(String appName, String packageName) {
        return getApi().query(packageName);
    }

    private Observable<ResponseBody> handleTargetJsonObjectAndDetail(Document document, AnalysisResult analysisResult) {
        if (document == null) {
            return Observable.error(new Exception(analysisResult.getApi() + ": search targetDocument is null"));
        }

        // 提取<a>标签的href属性
        Elements links = document.select("a[href]");
        String path = null;
        for (Element link : links) {
            if ("Click here".equals(link.text())) {
                path = link.attr("href");
                break;
            }
        }

        if (path == null) {
            return Observable.error(new Exception(analysisResult.getApi() + ": No matching link found"));
        }

        return getApi().detail(path);
    }

    @Override
    protected void detailResponseAnalysis(Document detailResponseDocument, AnalysisResult analysisResult) {
        if (detailResponseDocument == null) {
            analysisResult.setErrorMsg(analysisResult.getApi() + ": apkcombo api detail request success but document is null");
            analysisResult.setSuccess(false);
            return;
        }

        // 提取应用描述
        Element textDescriptionElement = detailResponseDocument.select(".text-description.ton").first();
        if (textDescriptionElement != null) {
            String appIntroduction = textDescriptionElement.text();
            analysisResult.getAppQueryResult().setAppIntroduction(appIntroduction);
        } else {
            analysisResult.getAppQueryResult().setAppIntroduction("");
        }

        // 提取应用图片
        Elements imageElements = detailResponseDocument.select(".gallery a[data-href]");
        List<String> pics = new ArrayList<>();
        for (Element img : imageElements) {
            String dataHref = img.attr("data-href");
            if (!dataHref.isEmpty()) {
                pics.add(dataHref);
            }
        }
        analysisResult.getAppQueryResult().setAppPictures(pics.toArray(new String[0]));
        analysisResult.setSuccess(true);
    }
}
