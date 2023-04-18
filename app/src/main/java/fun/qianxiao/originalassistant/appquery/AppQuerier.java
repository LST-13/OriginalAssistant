package fun.qianxiao.originalassistant.appquery;

import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

import fun.qianxiao.originalassistant.bean.AnalysisResult;
import fun.qianxiao.originalassistant.utils.net.ApiServiceManager;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * AppQuerier
 *
 * @Author QianXiao
 * @Date 2023/4/16
 */
public abstract class AppQuerier<T> implements IQuery {
    private final T apiService;

    public AppQuerier() {
        apiService = ApiServiceManager.getInstance().create(getGenericType());
    }

    protected T getApi() {
        return apiService;
    }

    @SuppressWarnings("unchecked")
    private Class<T> getGenericType() {
        return (Class<T>) ((ParameterizedType) Objects.requireNonNull(getClass().getGenericSuperclass())).getActualTypeArguments()[0];
    }

    @Override
    public void query(String appName, String packageName, OnAppQueryListener onAppQueryListener) {
        LogUtils.i("app query use " + getGenericType().getSimpleName(), appName, packageName);
        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.getAppQueryResult().setAppName(appName);
        analysisResult.getAppQueryResult().setPackageName(packageName);
        search(appName, packageName)
                .flatMap(new Function<ResponseBody, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(ResponseBody responseBody) throws Throwable {
                        return searchResponseAnalysisAndDetail(responseBody, analysisResult);
                    }
                })
                .map(new Function<ResponseBody, AnalysisResult>() {
                    @Override
                    public AnalysisResult apply(ResponseBody responseBody) throws Throwable {
                        detailResponseAnalysis(responseBody, analysisResult);
                        return analysisResult;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AnalysisResult>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull AnalysisResult analysisResult) {
                        if (analysisResult.isSuccess()) {
                            onAppQueryListener.onResult(OnAppQueryListener.QUERY_CODE_SUCCESS, null, analysisResult.getAppQueryResult());
                        } else {
                            onAppQueryListener.onResult(OnAppQueryListener.QUERY_CODE_FAILED, analysisResult.getErrorMsg(), null);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        ThreadUtils.runOnUiThread(() -> {
                            if (!TextUtils.isEmpty(analysisResult.getAppQueryResult().getAppIntroduction())) {
                                LogUtils.e(e.toString());
                                onAppQueryListener.onResult(OnAppQueryListener.QUERY_CODE_SUCCESS, e.getMessage(), analysisResult.getAppQueryResult());
                            } else {
                                onAppQueryListener.onResult(OnAppQueryListener.QUERY_CODE_FAILED, e.getMessage(), null);
                            }
                        });
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * search
     *
     * @param appName     appName
     * @param packageName packageName
     * @return {@link Observable<ResponseBody>}
     */
    protected abstract Observable<ResponseBody> search(String appName, String packageName);

    /**
     * searchResponseAnalysisAndDetail
     *
     * @param searchResponseBody responseBody
     * @param analysisResult     analysisResult
     * @return {@link Observable<ResponseBody>}
     */
    protected abstract Observable<ResponseBody> searchResponseAnalysisAndDetail(ResponseBody searchResponseBody, AnalysisResult analysisResult);

    /**
     * detailResponseAnalysis
     *
     * @param detailResponseBody responseBody
     * @param analysisResult     analysisResult
     */
    protected abstract void detailResponseAnalysis(ResponseBody detailResponseBody, AnalysisResult analysisResult);
}
