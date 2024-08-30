package com.plugin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import org.eclipse.core.resources.IFile;

import com.plugin.services.dto.AnalyserResponseDTO;
import com.plugin.services.dto.InconsistenciesResponseDTO;
import com.plugin.utils.Json2Obj;

public class InconsistencyAnalyserAPI {

	private final String urlBase = "http://localhost:8080/kafka";

	public AnalyserResponseDTO analyseFile(IFile iFile) {
		String url = urlBase + "/send";
		String charset = "UTF-8";
		String param = "/send";
		File textFile = iFile.getRawLocation().makeAbsolute().toFile();
		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
		HttpURLConnection connection = null;

		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			OutputStream output = connection.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

			// Send normal param.
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
			writer.append(CRLF).append(param).append(CRLF).flush();

			// Send text file.
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + textFile.getName() + "\"")
					.append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be
																						// saved in this charset!
			writer.append(CRLF).flush();
			Files.copy(textFile.toPath(), output);
			output.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

			// End of multipart/form-data.
			writer.append("--" + boundary + "--").append(CRLF).flush();

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

			return Json2Obj.deserializeObj(sb.toString(), AnalyserResponseDTO.class);
		} catch (Exception e) {
			System.out.println("analyseFile exception" + e.toString());
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return new AnalyserResponseDTO();
	}

	public InconsistenciesResponseDTO getInconsistenciesByClientId(String clientId) {
		HttpURLConnection connection = null;

		try {
			String urlWithClient = this.urlBase + "/inconsistencies/" + clientId;
			URL url = new URL(urlWithClient);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
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
