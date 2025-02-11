package fun.qianxiao.originalassistant.api.appquery;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;





public interface APKComboAppQueryApi extends AppQueryaApi {
    String API_NAME = "APKCombo";

    /**
     * hlx search
     *
     * @param keyword keyword
     * @return
     */
    @GET("https://apkcombo.app/zh")
    Observable<ResponseBody> query(@Query("packageName") String packageName);

    /**
     * hlx app info detail
     *
     * @param appId app_id
     * @return
     */
    @GET("https://apkcombo.app/zh")
    Observable<ResponseBody> detail(@Query("path") long path);
}
