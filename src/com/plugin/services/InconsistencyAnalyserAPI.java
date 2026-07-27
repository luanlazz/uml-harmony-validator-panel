package com.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.resources.IFile;

import com.plugin.exceptions.AnalyserException;
import com.plugin.i18n.MessageService;
import com.plugin.services.dto.AnalyserResponseDTO;
import com.plugin.services.dto.InconsistenciesResponse;
import com.plugin.utils.Json2Obj;
import com.plugin.utils.PluginLogger;

public class InconsistencyAnalyserAPI {

	private static final PluginLogger LOGGER = new PluginLogger(InconsistencyAnalyserAPI.class);

    private static final String PREF_KEY_BASE_URL = "base_url";
    private static final ContentType UML_CONTENT_TYPE = ContentType.create("application/xml", "UTF-8");
    
	private MessageService messageService;

	public InconsistencyAnalyserAPI() {
		this.messageService = MessageService.instance();
	}
	
	public static void setUrlBase(String url) {
        AppPreferences.put(PREF_KEY_BASE_URL, url);
    }

	public static String getUrlBase() {
		return AppPreferences.get(PREF_KEY_BASE_URL);
	}
	
	public AnalyserResponseDTO analyseFile(IFile iFile) throws AnalyserException {
        File file = iFile.getRawLocation().makeAbsolute().toFile();
        return analyseFile(file);
    }

    public AnalyserResponseDTO analyseFile(File file) throws AnalyserException {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return analyseBytes(bytes, file.getName());
        } catch (IOException e) {
            throw new AnalyserException("Could not read file: " + file.getAbsolutePath(), e);
        }
    }

	public AnalyserResponseDTO analyseBytes(byte[] modelBytes, String filename) throws AnalyserException {
        String url = getUrlBase();
        if (url == null || url.isBlank()) throw new AnalyserException(this.messageService.get("validation.service.url.not.configured"));

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(10000)
                .build();
        
        HttpEntity multipart = MultipartEntityBuilder.create().addBinaryBody("file", modelBytes, UML_CONTENT_TYPE, filename).build();

        HttpPost request = new HttpPost(url + "/model");
        request.setEntity(multipart);
        request.setConfig(requestConfig);
        request.setHeader("Accept-Language", messageService.getLocale().toString());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(request)) {

            int statusCode = resp.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(resp.getEntity(), "UTF-8");

            if (statusCode < 200 || statusCode > 299) throw new AnalyserException("Server returned HTTP " + statusCode + ": " + body);

            return Json2Obj.deserializeObj(body, AnalyserResponseDTO.class);
        } catch (IOException exception) {
        	LOGGER.error("HTTP POST request to [" + url + "] failed.", exception);
            throw new AnalyserException("HTTP POST request to [" + url + "] failed.", exception);
        }
    }

	public void streamInconsistencies(String clientId, SSEResultCallback callback) {
	    String urlBase = getUrlBase();
	    if (urlBase == null || urlBase.isBlank()) {
	        callback.onError(new AnalyserException(this.messageService.get("validation.service.url.not.configured")));
	        return;
	    }

	    try {
	        URL url = new URL(urlBase + "/stream/" + clientId);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setConnectTimeout(5000);
	        conn.addRequestProperty("Accept-Language", messageService.getLocale().toString());

	        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                if (line.startsWith("data:")) {
	                    String json = line.substring(5).trim();
	                    InconsistenciesResponse result = Json2Obj.deserializeObj(json, InconsistenciesResponse.class);
	                    callback.onResult(result);
	                    break;
	                }
	            }
	        } finally {
	            conn.disconnect();
	        }
	    } catch (Exception exception) {
	        LOGGER.error("SSE streaming error for clientId: " + clientId, exception);
	        callback.onError(exception);
	    }
	}
}
