import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import java.util.List;

public class Http {
    private HttpClient httpClient;

    public Http() {
        httpClient = HttpClients.custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
    }

    public String get(String url, String errMsg) throws Exception {
        try {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(httpGet);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new Exception(errMsg);
    }

    public String post(String url, List<NameValuePair> params, String errMsg) throws Exception {
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
            CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(httpPost);
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new Exception(errMsg);
    }
}
