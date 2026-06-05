package com.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.resources.IFile;

import com.plugin.exceptions.AnalyserException;
import com.plugin.i18n.MessageProvider;
import com.plugin.services.dto.AnalyserResponseDTO;
import com.plugin.services.dto.InconsistenciesResponseDTO;
import com.plugin.utils.Json2Obj;

public class InconsistencyAnalyserAPI {

    private static final String PREF_KEY_BASE_URL = "base_url";
    private static final ContentType UML_CONTENT_TYPE = ContentType.create("application/xml", "UTF-8");
    
	private MessageProvider messages;

	public InconsistencyAnalyserAPI() {
		this.messages = MessageProvider.instace();
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
        request.setHeader("Accept-Language", messages.getLocale().toString());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(request)) {

            int status = resp.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(resp.getEntity(), "UTF-8");

            if (status < 200 || status > 299) throw new AnalyserException("Server returned HTTP " + status + ": " + body);

            return Json2Obj.deserializeObj(body, AnalyserResponseDTO.class);
        } catch (IOException e) {
            throw new AnalyserException("HTTP request to [" + url + "] failed.", e);
        }
    }

	public InconsistenciesResponseDTO getInconsistenciesByClientId(String clientId) {
		HttpURLConnection connection = null;

		try {
			String urlWithClient = getUrlBase() + "/" + clientId;
			URL url = new URL(urlWithClient);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept-Language", this.messages.getLocale().toString());
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			int responseCode = connection.getResponseCode();

			StringBuilder sb = new StringBuilder();
			BufferedReader br = null;
			if (responseCode >= 200 && responseCode <= 299) {
				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			}

			String strCurrentLine;
			while ((strCurrentLine = br.readLine()) != null) {
				sb.append(strCurrentLine);
			}

			return Json2Obj.deserializeObj(sb.toString(), InconsistenciesResponseDTO.class);
		} catch (Exception e) {
			System.out.println("getInconsistenciesByClientId exception:" + e.toString());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return new InconsistenciesResponseDTO();
	}
}
