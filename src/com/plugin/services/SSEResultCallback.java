package com.plugin.services;

import com.plugin.services.dto.InconsistenciesResponse;

public interface SSEResultCallback {
    void onResult(InconsistenciesResponse result);
    void onError(Exception exception);
}
