package com.plugin.services;

import com.plugin.InconsistencyPanel;
import com.plugin.services.dto.InconsistenciesResponseDTO;

public class InconsistencyFetchAPI implements Runnable {

	private String clientId;
	private int maxRetries;
	private long retryDelay;
	private boolean success;

	private InconsistencyAnalyserAPI analyserService = new InconsistencyAnalyserAPI();

	public InconsistencyFetchAPI(String clientId, int maxRetries, long retryDelay) {
		this.clientId = clientId;
		this.maxRetries = maxRetries;
		this.retryDelay = retryDelay;
		this.success = false;
	}

	@Override
	public void run() {
		int retries = 0;
		InconsistenciesResponseDTO responseInconsistencies = null;

		int countSuccess = 0;
		int lastCountInconsistencies = 0;

		while (!success && retries < maxRetries) {
			try {
				responseInconsistencies = this.fecthInconsistenciesByClientId(this.clientId);

				if (responseInconsistencies.getSuccess()) {
					countSuccess++;
					int countInconsistencies = responseInconsistencies.getData().getInconsistencies().size();

					if (lastCountInconsistencies > 0 && lastCountInconsistencies == countInconsistencies) {
						success = true;
						break;
					}
					
					lastCountInconsistencies = countInconsistencies;					
				}

				try {
					retries++;
					Thread.sleep(retryDelay);
				} catch (InterruptedException ex) {
					System.out.println("InterruptedException: " + ex.getMessage());
					ex.printStackTrace();
				}
			} catch (Exception e) {
				System.out.println("Runnable Exception: " + e.getMessage());
			}
		}
				
		if (success || countSuccess > (this.maxRetries / 2)) {
			InconsistencyPanel.instace().updateViewData(responseInconsistencies.getData());
		} else {
			System.out.println("Tentativas esgotadas, não foi possível obter as inconsistências.");
		}
	}

	private InconsistenciesResponseDTO fecthInconsistenciesByClientId(String clientId) throws Exception {
		InconsistenciesResponseDTO response = analyserService.getInconsistenciesByClientId(clientId);
		return response;
	}
}
