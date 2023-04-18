package fun.qianxiao.originalassistant.api.appquery;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * TapTapAppQueryApi
 *
 * @Author QianXiao
 * @Date 2023/4/18
 */
public interface TapTapAppQueryApi {
    /**
     * taptap search
     * 'X-UA' is required, 'V=1&PN=TapTap&VN=2.53.1-rel.100000&LANG=zh_CN' in it is required
     *
     * @param keyword keyword
     * @return
     */
    @GET("api.taptapdada.com/search/v2/app?limit=10&X-UA=V=1%26PN=TapTap%26VN=2.53.1-rel.100000%26LANG=zh_CN&from=0")
    Observable<ResponseBody> query(@Query("kw") String keyword);
}
