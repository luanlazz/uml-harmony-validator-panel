package com.plugin.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import com.plugin.services.dto.InconsistenciesResponseDTO;
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

	public AnalyserResponseDTO analyseBytes(byte[] modelBytes, String filename)throws AnalyserException {
        String url = getUrlBase();
        if (url == null || url.isBlank()) throw new AnalyserException("Service URL is not configured. Set it via menu > settings.");

        HttpEntity multipart = MultipartEntityBuilder.create()
                .addBinaryBody(
                        "file",
                        modelBytes,
                        UML_CONTENT_TYPE,
                        filename)
                .build();

        HttpPost request = new HttpPost(url);
        request.setEntity(multipart);
        request.setHeader("Accept-Language", messageService.getLocale().toString());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(request)) {

            int statusCode = resp.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(resp.getEntity(), "UTF-8");

            if (statusCode < 200 || statusCode > 299) throw new AnalyserException("Server returned HTTP " + statusCode + ": " + body);

            return Json2Obj.deserializeObj(body, AnalyserResponseDTO.class);
        } catch (IOException e) {
            throw new AnalyserException("HTTP request to [" + url + "] failed.", e);
        }
    }

	public InconsistenciesResponseDTO getInconsistenciesByClientId(String clientId) throws AnalyserException {
	    String urlBase = getUrlBase();
	    if (urlBase == null || urlBase.isBlank()) throw new AnalyserException("Service URL is not configured. Set it via menu > settings.");

	    String url = urlBase + "/" + clientId;
	    HttpGet request = new HttpGet(url);
	    request.setHeader("Accept-Language", messageService.getLocale().toString());

	    try (CloseableHttpClient client = HttpClients.createDefault();
	         CloseableHttpResponse resp = client.execute(request)) {

	        int statusCode = resp.getStatusLine().getStatusCode();
	        String body = EntityUtils.toString(resp.getEntity(), "UTF-8");

	        if (statusCode < 200 || statusCode > 299) throw new AnalyserException("Server returned HTTP " + statusCode + ": " + body);

	        return Json2Obj.deserializeObj(body, InconsistenciesResponseDTO.class);
	    } catch (IOException exception) {
	        LOGGER.error("HTTP GET request to [" + url + "] failed.", exception);
	        throw new AnalyserException("HTTP GET request to [" + url + "] failed.", exception);
	    }
	}
}
