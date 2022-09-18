package com.github.saserg.webclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.os.HandlerCompat;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttpClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class RestClientBuilder<T extends Serializable> {
    private T instance;
    private OnSuccess<T> onSuccess;
    private OnError onError;
    private final Class<T> responseType;
    private String queryUrl = "";
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;
    private final Message message = Message.obtain();
    private final Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());
    private final HttpHeaders httpHeaders = new HttpHeaders();

    private RestClientBuilder(Class<T> clazz) {
        this.responseType = clazz;
        this.restTemplate = new RestTemplate();
        this.executorService = newCachedThreadPool();
        try {
            this.instance = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public RestClientBuilder<T> url(String url) {
        this.queryUrl = url;
        return this;
    }

    public RestClientBuilder<T> uri(String uri) {
        this.queryUrl = queryUrl.concat(uri);
        return this;
    }

    public RestClientBuilder<T> auth(HttpBasicAuthentication basicAuthentication) {
        this.httpHeaders.setAuthorization(basicAuthentication);
        return this;
    }

    public RestClientBuilder<T> auth(String jwt) {
        this.httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        return this;
    }

    public RestClientBuilder<T> auth(String header, String token) {
        this.httpHeaders.add(HttpHeaders.AUTHORIZATION, header + " " + token);
        return this;
    }

    public RestClientBuilder<T> headers(Map<String, String> headers) {
        this.httpHeaders.setAll(headers);
        return this;
    }

    public RestClientBuilder<T> get() {
        executorService.execute(() -> {
            HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<?> responseEntity = null;
            try {
                responseEntity = restTemplate.exchange(queryUrl, HttpMethod.GET, httpEntity, responseType);
            } catch (HttpClientErrorException e) {
                responseEntity = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
            } catch (HttpServerErrorException e) {
                responseEntity = new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.INTERNAL_SERVER_ERROR);
            } finally {
                Bundle bundle = new Bundle();
                if (responseEntity != null) {
                    execute(responseEntity, bundle);
                }
                message.setTarget(handler);
                message.setData(bundle);
                handleMessage(message);
            }
        });
        return this;
    }

    public RestClientBuilder<T> get(Object... params) {
        executorService.execute(() -> {
            HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<?> responseEntity = null;
            try {
                responseEntity = restTemplate.exchange(queryUrl, HttpMethod.GET, httpEntity, responseType, params);
            } catch (HttpClientErrorException e) {
                responseEntity = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
            } catch (HttpServerErrorException e) {
                responseEntity = new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.INTERNAL_SERVER_ERROR);
            } finally {
                Bundle bundle = new Bundle();
                if (responseEntity != null) {
                    execute(responseEntity, bundle);
                }
                message.setTarget(handler);
                message.setData(bundle);
                handleMessage(message);
            }
        });
        return this;
    }

    public RestClientBuilder<T> post(MultiValueMap<String, Object> body) {
        executorService.execute(() -> {
            this.httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, httpHeaders);
            postExecute(httpEntity);
        });
        return this;
    }

    public RestClientBuilder<T> post(Map<String, Object> body) {
        executorService.execute(() -> {
            this.httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, httpHeaders);
            postExecute(httpEntity);
        });
        return this;
    }

    public RestClientBuilder<T> post(Map<String, String> body, String key, MediaType mediaType, ByteArrayResource... resources) {
        executorService.execute(() -> {
            MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
            this.httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpHeaders imageHeaders = new HttpHeaders();
            imageHeaders.setContentType(mediaType);
            for (ByteArrayResource resource : resources) {
                if (resource != null) {
                    HttpEntity<ByteArrayResource> imageResources = new HttpEntity<>(resource, imageHeaders);
                    requestBody.add(key, imageResources);
                }
            }
            HttpHeaders textHeaders = new HttpHeaders();
            textHeaders.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            for (Map.Entry<String, String> entry : body.entrySet()) {
                HttpEntity<String> stringEntity = new HttpEntity<>(entry.getValue(), textHeaders);
                requestBody.add(entry.getKey(), stringEntity);
            }
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(requestBody, httpHeaders);
            postExecute(httpEntity);
        });
        return this;
    }

    public RestClientBuilder<T> put(Map<String, Object> body) {
        executorService.execute(() -> {
            this.httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, httpHeaders);
            putExecute(httpEntity);
        });
        return this;
    }

    public RestClientBuilder<T> put(MultiValueMap<String, Object> body) {
        executorService.execute(() -> {
            this.httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, httpHeaders);
            putExecute(httpEntity);
        });
        return this;
    }

    public RestClientBuilder<T> put(Map<String, String> body, String key, MediaType mediaType, ByteArrayResource... resources) {
        executorService.execute(() -> {
            MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
            this.httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpHeaders imageHeaders = new HttpHeaders();
            imageHeaders.setContentType(mediaType);
            for (ByteArrayResource resource : resources) {
                if (resource != null) {
                    HttpEntity<ByteArrayResource> imageResources = new HttpEntity<>(resource, imageHeaders);
                    requestBody.add(key, imageResources);
                }
            }
            HttpHeaders textHeaders = new HttpHeaders();
            textHeaders.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
            for (Map.Entry<String, String> entry : body.entrySet()) {
                HttpEntity<String> stringEntity = new HttpEntity<>(entry.getValue(), textHeaders);
                requestBody.add(entry.getKey(), stringEntity);
            }
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(requestBody, httpHeaders);
            putExecute(httpEntity);
        });
        return this;
    }

    public RestClientBuilder<T> ssl(InputStream x509Cert) {
        this.restTemplate.setRequestFactory(new OkHttpClientHttpRequestFactory(SSLHttpClient.getInstance(x509Cert)));
        return this;
    }

    public RestClientBuilder<T> success(OnSuccess<T> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public RestClientBuilder<T> error(OnError onError) {
        this.onError = onError;
        return this;
    }

    private void handleMessage(Message message) {
        handler.post(() -> {
            HttpStatus httpStatus = HttpStatus.valueOf(message.getData().getInt("Status"));
            if (httpStatus.is2xxSuccessful()) {
                instance = responseType.cast(message.getData().getSerializable("Object"));
                if (onSuccess != null)
                    onSuccess.success(instance, (HttpHeaders) message.getData().getSerializable("Headers"), httpStatus);
            } else if (httpStatus.is4xxClientError() || httpStatus.is5xxServerError()) {
                if (onError != null)
                    onError.error(httpStatus, message.getData().getString("Message"));
            }
            executorService.shutdown();
        });
    }

    private void execute(ResponseEntity<?> responseEntity, Bundle bundle) {
        HttpStatus httpStatus = responseEntity.getStatusCode();
        bundle.putInt("Status", responseEntity.getStatusCode().value());
        if (httpStatus.is2xxSuccessful()) {
            bundle.putSerializable("Object", (Serializable) responseEntity.getBody());
            bundle.putSerializable("Headers", responseEntity.getHeaders());
        } else {
            bundle.putString("Message", (String) responseEntity.getBody());
        }
    }

    private void postExecute(HttpEntity<?> httpEntity) {
        ResponseEntity<?> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(queryUrl, HttpMethod.POST, httpEntity, responseType);
        } catch (HttpClientErrorException e) {
            responseEntity = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (HttpServerErrorException e) {
            responseEntity = new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            Bundle bundle = new Bundle();
            if (responseEntity != null) {
                execute(responseEntity, bundle);
            }
            message.setTarget(handler);
            message.setData(bundle);
            handleMessage(message);
        }
    }

    private void putExecute(HttpEntity<?> httpEntity) {
        ResponseEntity<?> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(queryUrl, HttpMethod.PUT, httpEntity, responseType);
        } catch (HttpClientErrorException e) {
            responseEntity = new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (HttpServerErrorException e) {
            responseEntity = new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            Bundle bundle = new Bundle();
            if (responseEntity != null) {
                execute(responseEntity, bundle);
            }
            message.setTarget(handler);
            message.setData(bundle);
            handleMessage(message);
        }
    }

    public static <T extends Serializable> RestClientBuilder<T> build(Class<T> clazz) {
        return new RestClientBuilder<>(clazz);
    }
}
