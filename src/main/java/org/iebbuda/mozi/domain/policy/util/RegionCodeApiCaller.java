package org.iebbuda.mozi.domain.policy.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@Component
@Log4j2
public class RegionCodeApiCaller {

    @Value("${zip.api.key}")
    private String apiKey;

    @Value("${zip.api.url}")
    private String apiUrl;

    // 요청 URL 생성 (인증키 포함)
    public String getRequestUrl(int page, int perPage) {
        try {
            String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            return apiUrl + "?serviceKey=" + encodedKey + "&page=" + page + "&perPage=" + perPage + "&type=json";
        } catch (Exception e) {
            throw new RuntimeException("인증키 인코딩 실패", e);
        }
    }

    // 단일 페이지 zip 코드 목록 조회
    public Map<String, Map<String, String>> fetchZipCodes(int page, int perPage) {
        Map<String, Map<String, String>> regionMap = new HashMap<>();

        try {
            String fullUrl = getRequestUrl(page, perPage);
            log.info("RegionCode 요청 URL: {}", fullUrl);

            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                parseJson(response.toString(), regionMap);
            } else {
                log.warn("RegionCode HTTP 오류 코드: {}", responseCode);
            }

        } catch (Exception e) {
            log.error("RegionCode API 호출 중 예외", e);
        }

        return regionMap;
    }

    // 전체 페이지 zip 코드 목록 반복 조회 후 병합
    public Map<String, Map<String, String>> fetchAllZipCodes(int totalPages, int perPage) {
        Map<String, Map<String, String>> totalMap = new HashMap<>();

        for (int page = 1; page <= totalPages; page++) {
            log.info("[ZipCd] ({}/{}) Page Processing...", page, totalPages);

            Map<String, Map<String, String>> pageMap = fetchZipCodes(page, perPage);

            // merge
            for (Map.Entry<String, Map<String, String>> entry : pageMap.entrySet()) {
                String sido = entry.getKey();
                Map<String, String> sigunguMap = entry.getValue();

                totalMap.computeIfAbsent(sido, k -> new TreeMap<>())
                        .putAll(sigunguMap); // 중복 제거됨
            }

            try {
                Thread.sleep(300); // 과도한 요청 방지용 딜레이
            } catch (InterruptedException ignored) {}
        }

        return totalMap;
    }

    // JSON 응답을 파싱해 지역 Map에 저장
    private void parseJson(String json, Map<String, Map<String, String>> regionMap) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray data = root.getAsJsonArray("data");

            for (JsonElement elem : data) {
                JsonObject obj = elem.getAsJsonObject();

                String address = obj.get("법정동명").getAsString();      // "부산광역시 서구 남부민동"
                String code = obj.get("법정동코드").getAsString();       // 10자리
                String status = obj.get("폐지여부").getAsString();       // 존재 or 폐지

                if (!"존재".equals(status)) continue;

                String[] parts = address.split(" ");
                if (parts.length < 2) continue;

                String sido = parts[0];

                // 세종시 예외처리
                if ("세종특별자치시".equals(sido)) {
                    regionMap.computeIfAbsent(sido, k -> new TreeMap<>())
                            .put("세종특별자치시", code.substring(0, 5));
                    continue;
                }

                String sigungu;
                if (parts[1].contains("시") && parts.length > 2) {
                    if (parts[2].endsWith("구") || parts[2].endsWith("군")) {
                        sigungu = parts[1] + " " + parts[2];  // ex: 수원시 영통구
                    } else {
                        sigungu = parts[1];                  // ex: 익산시
                    }
                } else {
                    sigungu = parts[1];                      // ex: 고성군
                }

                // 읍/면/동 저장 안 함
                if (sigungu.endsWith("읍") || sigungu.endsWith("면") || sigungu.endsWith("동")) continue;

                regionMap.computeIfAbsent(sido, k -> new TreeMap<>())
                        .put(sigungu, code.substring(0, 5)); // 5자리만 사용
            }

        } catch (Exception e) {
            log.error("RegionCode JSON 파싱 오류", e);
        }
    }

}
